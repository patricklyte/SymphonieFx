package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.MultichannelEnum;
import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SymphonieInstrument;
import symreader.OldSampleImporter;
import symreader.VirtualSampleBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class NewSampleLoader {
    static protected org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MethodHandles.lookup().lookupClass());
    TimeMeasure timeMeasure = new TimeMeasure();

    private static VirtualSampleBuilder postprocessor = new VirtualSampleBuilder();
    public void loadNewSample(final SymphonieInstrument instrument, final byte[] sampleData, final int sampleLength, final int ActualSampleIndex, final Song MySong, boolean javeImporterEnabled) {
        testNewSampleLoader(instrument, sampleData, sampleLength);
        timeMeasure.start("Old Sample Loader");
        OldSampleImporter importer = instrument.getSampleImporter();
        importer.analyseAndImport(instrument, sampleData, sampleLength, javeImporterEnabled);
        initInstrument(instrument, importer);
        updateOtherStereoChannel(instrument, ActualSampleIndex, MySong, importer);
        timeMeasure.stop("Old Sample Loader");
        logger.debug("Old Sample Loader:"+timeMeasure.getDiffString("Old Sample Loader"));
        postprocessor.postProcessSample(instrument);
    }

    private void updateOtherStereoChannel(SymphonieInstrument instrument, int ActualSampleIndex, Song MySong, OldSampleImporter importer) {
        if (instrument.getMultiChannel().equals(MultichannelEnum.StereoL)) {
            if (instrument.getSampleImporter().isStereo()) {
                SymphonieInstrument siRight = MySong.getInstrument(ActualSampleIndex + 1);
                siRight.setSamplePool(instrument.getSampleImporter().getSamplePoolRight());
                siRight.getSamplePool().initLoopData(instrument.isLoopEnabled(), instrument.getLoopStart(), instrument.getLoopLength(), instrument.getNumberOfLoops());
                siRight.setHasContent(true);
                siRight.setMultiChannel(MultichannelEnum.StereoR);
                siRight.setName(instrument.getName());
                siRight.setTune(instrument.getTune());
                siRight.setSampleResolution(importer.getSampleResolutionBit());
                siRight.setSampleImporter(instrument.getSampleImporter());
                siRight.setIndex(instrument.getIndex() + 1);
            } else {
                // error Stereo Sample not correctly recognized
            }
        }
    }

    private void initInstrument(SymphonieInstrument instrument, OldSampleImporter importer) {
        instrument.setSamplePool(importer.getSamplePool());
        instrument.getSamplePool().initLoopData(instrument.isLoopEnabled(), instrument.getLoopStart(), instrument.getLoopLength(), instrument.getNumberOfLoops());
        instrument.setHasContent(true);
        if (importer.isStereo()) {
            instrument.setMultiChannel(MultichannelEnum.StereoL);
        } else {
            instrument.setMultiChannel(MultichannelEnum.Mono);
        }
        instrument.setSampleResolution(importer.getSampleResolutionBit());
        if(instrument.getSampleImporter().getSampledFrequency() > 40000) {
            instrument.setTune(0);
        }
    }

    void testNewSampleLoader(final SymphonieInstrument instrument, final byte[] sampleData, final int sampleLength) {
        try {
            timeMeasure.start("NewSampleLoader");
            // test iff
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sampleData, 0, sampleLength);
            NewSampleImporter importer = new SampleImporterIff(byteArrayInputStream);

            // test wav
            byteArrayInputStream = new ByteArrayInputStream(sampleData, 0, sampleLength);
            importer = new SampleImporterWav(byteArrayInputStream);
            timeMeasure.stop("NewSampleLoader");
            logger.debug("NewSampleLoader:" + timeMeasure.getDiffString("NewSampleLoader"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
