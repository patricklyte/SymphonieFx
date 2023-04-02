package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.MultichannelEnum;
import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SymphonieInstrument;
import jakarta.xml.bind.JAXB;
import org.apache.log4j.Logger;
import symreader.SongIO;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static ch.meng.symphoniefx.SharedStatic.directoryDelimiter;

public class SongSaver {
    public static final String SYMMOD2_SUFFIX = ".symmod2.zip";
    public static final String XML_SONG_NAME = "SymphonieSong_v1.00.xml";
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    private static final int IO_BUFFER_SIZE = 4096;

    Song song;

    String saveSong(Song song) {
        String savingMessage;
        String savedSongFilePath = "";
        this.song = song;
        if(!song.isContentLoaded()) {
            return "Nothing to save. saveSong Aborted.";
        }
        try {
            String directoryName = getTempDirectoryName(song.getName());
            File directory = new File(directoryName);
            directory.mkdir();
            exportAllSamples(directoryName);
            File xmlFile = new File(directoryName + directoryDelimiter() + XML_SONG_NAME);
            saveVstSetupToInstruments();
            JAXB.marshal(song, xmlFile);
            savedSongFilePath = zipDirectory(directoryName);
            savingMessage = "Saving "+savedSongFilePath +" done";
            this.savedPath = savedSongFilePath;
            xmlSaveTest(song);
        } catch (IOException exception) {
            savingMessage = "Error:Saving "+savedSongFilePath +" failed " + exception.getMessage();
            logError(logger, exception);
        }
        return savingMessage;
    }

    private void saveVstSetupToInstruments() {
        for(SymphonieInstrument instrument : song.getVstInstrumentsAsList()) {
            instrument.getVstSetup().setDllFilename(instrument.getName());
            if(instrument.getVstManager().getVst().acceptsProgramsAsChunks())
                instrument.getVstSetup().setProgramChunk(instrument.getVstManager().getVst().getProgramChunk());
            if(instrument.getVstManager().getVst().numPrograms()>1)
                instrument.getVstSetup().setProgramNr(instrument.getVstManager().getVst().getProgram());
        }
    }

    String savedPath;
    String getSavedSongPath() {
        return savedPath;
    }

    boolean xmlSaveTest(Song song) {
        StringWriter sw = new StringWriter();
        JAXB.marshal(song, sw);
        StringReader reader = new StringReader(sw.toString());
        Song marshalledSong = JAXB.unmarshal(reader, Song.class);
        List<String> errors = song.compare(marshalledSong);
        if(!errors.isEmpty()) {
            logger.error("JAXB converting errors");
        }
        return errors.isEmpty();
    }

    private void logError(Logger logger, Exception exception) {
        logger.error(exception.getMessage());
        exception.printStackTrace();
    }

    private void exportAllSamples(String directoryName) {
        if (!song.isContentLoaded()) return;
        song.getInstrumentsAsList().stream()
                .filter(instrument -> !instrument.isVirtualSample())
                .forEach(instrument -> exportSample(instrument, directoryName));
    }

    private void exportSample(final SymphonieInstrument instrument, String directoryName) {
        try {
            if (instrument.getMultiChannel().equals(MultichannelEnum.StereoR)) return;
            if (!instrument.hasContent()) return;
            File file = new File(directoryName + directoryDelimiter()
                    + buildSaveInstrumentName(instrument));

            SongIO songIO = new SongIO();
            songIO.exportSample(song, file, instrument.getIndex());
        } catch (Exception exception) {
            logger.error("export " + instrument.getShortDescription() + " failed " + exception.getMessage());
        }
    }

    String buildSaveInstrumentName(final SymphonieInstrument instrument) {
        String instrumentName = "Instr-" + instrument.getIndex()
                + "-" +  cleanFileName(instrument.getSongSaveSampleName());
        if(!instrumentName.toLowerCase().endsWith(instrument.getSampleImporter().getFileSuffix().toLowerCase())) {
            instrumentName += instrument.getSampleImporter().getFileSuffix();
        }
        instrument.setFilename(instrumentName);
        return instrumentName;
    }

    String cleanFileName(String instrumentName) {
        instrumentName = instrumentName.replaceAll(":", "");
        instrumentName = instrumentName.replaceAll("\\*", "");
        instrumentName = instrumentName.replaceAll("\\\\", " ");
        instrumentName = instrumentName.replaceAll("/", " ");
        return instrumentName;
    }

    String setNewFormatSuffix(String text) {
        int pos = text.toLowerCase().lastIndexOf(".symmod");
        if(pos<1) return text;
        return text.substring(0, pos) + ".symmod2";
    }

    String getTempDirectoryName(String text) {
        int pos = text.toLowerCase().lastIndexOf(".symmod2");
        if(pos>1) text = text.substring(0, pos);
        pos = text.toLowerCase().lastIndexOf(".symmod");
        if(pos>1) text = text.substring(0, pos);
        return text + "-temp";
    }
    public String zipDirectory(String sourceFile) throws IOException {
        String finalFilename = sourceFile.replace("-temp", "");
        FileOutputStream zipFileStream = new FileOutputStream(finalFilename + SYMMOD2_SUFFIX);
        ZipOutputStream zipOut = new ZipOutputStream(zipFileStream);
        File fileToZip = new File(sourceFile);
        if(!fileToZip.exists()) {
            logger.error("File not found ["+sourceFile+"]");
        }
        if(!fileToZip.canRead()) {
            logger.error("Cannot read from ["+sourceFile+"]");
        }
        zipFile(fileToZip, fileToZip.getName(), zipOut,true);
        zipOut.close();
        zipFileStream.close();
        return finalFilename + SYMMOD2_SUFFIX;
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, boolean flatStructure) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if(!flatStructure) {
                if (fileName.endsWith("/")) {
                    zipOut.putNextEntry(new ZipEntry(fileName));
                    zipOut.closeEntry();
                } else {
                    zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                    zipOut.closeEntry();
                }
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut, flatStructure);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        fileName = cutDirectoryPath(fileName);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[IO_BUFFER_SIZE];
        int length;
        while ((length = fis.read(bytes)) >= 0) zipOut.write(bytes, 0, length);
        fis.close();
    }

    private String cutDirectoryPath(String fileName) {
        int position = fileName.lastIndexOf(SharedStatic.directoryDelimiter());
        if(position>0) fileName = fileName.substring(position+1);
        position = fileName.lastIndexOf("/");
        if(position>0) fileName = fileName.substring(position+1);
        return fileName;
    }

}
