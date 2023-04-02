package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.MultichannelEnum;
import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SymphonieInstrument;
import jakarta.xml.bind.JAXB;
import org.apache.log4j.Logger;
import symreader.SongIO;
import symreader.VirtualSampleBuilder;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static ch.meng.symphoniefx.SharedStatic.directoryDelimiter;
import static ch.meng.symphoniefx.SongSaver.XML_SONG_NAME;

public class SongLoader {
    public static final String SYMMOD2_SUFFIX = ".symmod2.zip";
    public static final String SYMMOD2_SONG_SUFFIX = ".symmod2.xml";
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    private static final int IO_BUFFER_SIZE = 4096;

    Song song;
    int bufferLenInSamples;
    int mixfrequency;

    Song loadSong(File modfile, int bufferLenInSamples, int mixfrequency) {
        this.song = new Song();
        this.bufferLenInSamples = bufferLenInSamples;
        this.mixfrequency = mixfrequency;
        String savedSongFilePath = "";
        try {
            unzipFiles(modfile);
            logger.debug("Loading " + savedSongFilePath + " done");
        } catch (Exception exception) {
            logger.error("Error:Loading " + savedSongFilePath + " failed " + exception.getMessage());
            logError(logger, exception);
        }
        return song;
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
                + "-" + cleanFileName(instrument.getSongSaveSampleName());
        if (!instrumentName.contains(".")) instrumentName += instrument.getSampleImporter().getFileSuffix();
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

    TimeMeasure timer = new TimeMeasure();
    SongLoadPart songEntry;
    List<SongLoadPart> zipentries = new Vector<>();

    public String unzipFiles(File fileToUnzip) throws IOException {
        if (!fileToUnzip.exists()) {
            logger.error("File not found [" + fileToUnzip.getPath() + "]");
        }
        if (!fileToUnzip.canRead()) {
            logger.error("Cannot read from [" + fileToUnzip.getPath() + "]");
        }
        timer.start("unzip");
        FileInputStream zipFileStream = new FileInputStream(fileToUnzip.getPath());
        ZipInputStream zipInputStream = new ZipInputStream(zipFileStream);
        ZipEntry zipEntry;
        String directoryName = fileToUnzip.getParent() + SharedStatic.directoryDelimiter() + "temp";
        File directory = new File(directoryName);
        directory.mkdir();
        SongLoadPart part;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            part = unzip(directory, zipEntry, zipInputStream);
            if (part == null) continue;
            if (isSongName(zipEntry)) {
                songEntry = part;
                logger.debug("Symphonie Song found:" + part.getExtractedPath());
            } else {
                zipentries.add(part);
                logger.debug("Part found:" + part.getExtractedPath());
            }
        }
        timer.stop("unzip");
        if (songEntry != null) {
            logger.debug("JAXB unmarshal:");
            File songFile = new File(songEntry.getExtractedPath());
            timer.start("JAXB");
            song = JAXB.unmarshal(songFile, Song.class);
            timer.stop("JAXB");
            timer.start("loadSamples");
            loadSamples();
            timer.stop("loadSamples");
            timer.start("virtualSampleBuilder");
            VirtualSampleBuilder virtualSampleBuilder = new VirtualSampleBuilder();
            virtualSampleBuilder.buildVirtualSamples(song);
            timer.stop("virtualSampleBuilder");
            loadVSTInstruments();
            logger.debug("JAXB unmarshal:");
            song.setName(fileToUnzip.getPath());
        }
        zipInputStream.close();
        zipFileStream.close();
        logger.debug(getSpeedMeasure());
        return "";
    }

    void loadVSTInstruments() {
        for (SymphonieInstrument instrument : song.getVstInstrumentsAsList()) {
            File file = new File(instrument.getName());
            if (!file.exists()) {
                logger.debug("VST Instrument not found:" + instrument.getName());
                return;
            }
            instrument.loadVstInstrument(file, bufferLenInSamples, mixfrequency);
            if(instrument.getVstManager() == null) {
                logger.debug("Error:VstManager null. skipping vst loading");
                continue;
            }
            if(instrument.getVstManager().getVst() == null) {
                logger.debug("Error:Vst null. skipping vst loading");
                continue;
            }
            if(!instrument.getVstManager().getVst().isNativeComponentLoaded()) {
                logger.debug("Error:NativeComponent not loaded. skipping vst loading");
                continue;
            }
            if (instrument.getVstSetup().getProgramChunk() != null && instrument.getVstSetup().getProgramChunk().length > 0) {
                instrument.getVstManager().getVst().setProgramChunk(instrument.getVstSetup().getProgramChunk());
                logger.debug("ProgramChunk loaded and set");
            } else if (instrument.getVstManager().getVst().numPrograms() > 0 && instrument.getVstSetup().getProgramNr() > 0) {
                instrument.getVstManager().getVst().setProgram(instrument.getVstSetup().getProgramNr());
                logger.debug("Program set to "+instrument.getVstSetup().getProgramNr());
            }
            song.getVstInstruments().add(instrument);
            instrument.setHasContent(true);
        }
    }

    String getSpeedMeasure() {
        StringBuilder text = new StringBuilder();
        text.append("Load Statistics:")
                .append("unzip:").append(timer.getDiffString("unzip"))
                .append("JAXB:").append(timer.getDiffString("JAXB"))
                .append("loadSamples:").append(timer.getDiffString("loadSamples"))
                .append("virtualSampleBuilder:").append(timer.getDiffString("virtualSampleBuilder"));
        return text.toString();
    }

    SongIO songIO = new SongIO();

    void loadSamples() throws IOException {
        for (SymphonieInstrument instrument : song.getInstrumentsAsList()) {
            if (instrument == null || instrument.isVirtualSample() || instrument.getMultiChannel().equals(MultichannelEnum.StereoR))
                continue;
            int index = instrument.getIndex();
            if (index < 0) continue;
            String filename = getTempSampleName(instrument.getIndex());
            if (filename.isEmpty()) continue;
            logger.debug("Loading Sample:" + instrument.getShortDescription());
            File sampleFile = new File(filename);
            logger.debug("Loading Sample:" + sampleFile.getPath());
            if (!sampleFile.exists()) {
                logger.debug("Error:File not found:" + sampleFile.getPath());
                continue;
            }
            if (!sampleFile.canRead()) {
                logger.debug("Error:Cannot read from:" + sampleFile.getPath());
                continue;
            }
            FileInputStream basefi = new FileInputStream(sampleFile);
            long SampleLenByte = sampleFile.length();
            logger.debug("Filelength:" + SampleLenByte);
            if (SampleLenByte < 1) continue;
            //todo: check illegal size
            byte[] Samples = new byte[((int) SampleLenByte)];
            basefi.read(Samples);
            songIO.loadSample(instrument, Samples, Samples.length, index, song);
            song.getInstrument(index).setHasContent(true);
        }
    }

    String getTempSampleName(int index) {
        for (SongLoadPart part : zipentries) {
            if (index == part.getInstrumentIndex()) return part.getExtractedPath();
        }
        return "";
    }

    boolean isSongName(ZipEntry zipEntry) {
        return zipEntry.getName().equals(XML_SONG_NAME);
    }

    byte[] buffer = new byte[4096];

    SongLoadPart unzip(File directory, ZipEntry zipEntry, ZipInputStream stream) {
        if (zipEntry == null) return null; // could be samples only
        String tempFilename = directory + SharedStatic.directoryDelimiter() + zipEntry.getName();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFilename);
            logger.debug("Unzipping:" + tempFilename);
            BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);
            int len;
            while ((len = stream.read(buffer)) > 0) bos.write(buffer, 0, len);
            bos.flush();
            bos.close();
            fos.flush();
            fos.close();
            logger.debug("Unzipping [" + tempFilename + "]");
            return new SongLoadPart(zipEntry, tempFilename);
        } catch (Exception exception) {
            logger.debug("Error:" + exception.getMessage());
            exception.printStackTrace();
        }
        return null;
    }

//    void loadSong(File directory, ZipEntry zipEntry, ZipInputStream stream) {
//        if(zipEntry == null) return; // could be samples only
//        String tempFilename = directory + SharedStatic.directoryDelimiter() + zipEntry.getName();
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(tempFilename);
//            BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);
//            int len;
//            while ((len = stream.read(buffer)) > 0) {
//                bos.write(buffer, 0, len);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, boolean flatStructure) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (!flatStructure) {
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
        if (position > 0) fileName = fileName.substring(position + 1);
        position = fileName.lastIndexOf("/");
        if (position > 0) fileName = fileName.substring(position + 1);
        return fileName;
    }

}
