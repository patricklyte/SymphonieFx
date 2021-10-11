package ch.meng.symphoniefx.rendering;

import ch.meng.symphoniefx.TimeMeasure;
import ch.meng.symphoniefx.mixer.VoiceExpander;
import ch.meng.symphoniefx.song.MultichannelEnum;
import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SymphonieInstrument;
import io.nayuki.flac.common.StreamInfo;
import io.nayuki.flac.encode.BitOutputStream;
import io.nayuki.flac.encode.FlacEncoder;
import io.nayuki.flac.encode.RandomAccessFileOutputStream;
import io.nayuki.flac.encode.SubframeEncoder;
import org.apache.log4j.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class FileRenderer {
    public static final int PRERENDER_BUFFER_LEN = 512;
    public static final int RENDER_BUFFER_LEN = 512;
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    public static final String RENDER_TIMER_ID = "render";
    private int renderFrequency = 44100;
    private String filepath;
    private boolean shutdown = false;
    private VoiceExpander voiceExpander;
    private double renderVolume;
    private double maxAmplitudeOfSong;

    public VoiceExpander getVoiceExpander() {
        return voiceExpander;
    }

    void notifyUpdate() {
    }

    void shutdown() {
        shutdown = true;
    }

    private VoiceExpander oldvoiceExpander = null;
    boolean cloneAudioSetup;
    boolean onlySampleName;
    RenderFileFormat renderFileFormat;

    void renderToFile(Song song,
                      int outputFrequency,
                      double maxAmplitudeOfSong,
                      String filepath,
                      RenderFileMode renderFileMode,
                      boolean cloneAudioSetup,
                      double renderVolume,
                      boolean onlySampleName,
                      RenderFileFormat renderFileFormat) {

        this.maxAmplitudeOfSong = maxAmplitudeOfSong;
        this.renderVolume = renderVolume;
        oldvoiceExpander = song.getLinkedVoiceExpander();
        this.cloneAudioSetup = cloneAudioSetup;
        this.onlySampleName = onlySampleName;
        this.renderFileFormat = renderFileFormat;

        song.stopSong();
        if (oldvoiceExpander != null) oldvoiceExpander.stopAllVoices();
        this.filepath = filepath;
        song.muteAllInstruments(false);
        switch (renderFileMode) {
            case SingleFile: {
                print("Rendering " + song.getName());
                VoiceExpander voiceExpander = getVoiceExpander(song, outputFrequency);
                renderSingleFileStereo(song, voiceExpander, "");
            }
            break;
            case FilePerInstrument:
                renderFilePerInstrument(song, outputFrequency);
                break;
            case FilePerStereoChannel:
                renderFilePerStereoChannel(song, outputFrequency);
                break;
            case FilePerGroup:
                renderFilePerGroup(song, outputFrequency);
                break;
            case AllNonMuted:
                VoiceExpander voiceExpander = getVoiceExpander(song, outputFrequency);
                song.restoreInstrumentMuteState();
                renderSingleFileStereo(song, voiceExpander, "");
                break;
        }
        song.muteAllInstruments(false);
        if (oldvoiceExpander != null) oldvoiceExpander.stopAllVoices();
        song.setLinkedVoiceExpander(oldvoiceExpander);
    }

    double calcMaximum(Song song) {
        voiceExpander = getVoiceExpander(song, 44100);
        voiceExpander.setMasterVolume(100);
        return renderNullStereo(song, voiceExpander);
    }

    private VoiceExpander getVoiceExpander(Song song, int outputFrequency) {
        voiceExpander = new VoiceExpander();
        voiceExpander.initAllVoices();
        voiceExpander.setMasterVolume(this.renderVolume);
        song.setLinkedVoiceExpander(voiceExpander);
        voiceExpander.initMixSystem(true); // todo: make a copy of song, for independent rendering
        voiceExpander.setMixFrequency(outputFrequency);
        this.renderFrequency = outputFrequency;
        voiceExpander.setSong(song);
        song.stopSong();
        voiceExpander.stopAllVoices();
        voiceExpander.setSong(song);
        voiceExpander.setSongSpeed(song.getBpm(), song.getPositionSpeed());
        if (cloneAudioSetup) oldvoiceExpander.cloneAudioSetup(voiceExpander);
        return voiceExpander;
    }

    private void renderFilePerGroup(final Song song, final int outputFrequency) {
        print("Rendering " + song.getName());
        for (String group : song.getInstrumentGroups()) {
            song.muteInstrumentsOfGroup(group, false);
            String suffix = group;
            VoiceExpander voiceExpander = getVoiceExpander(song, outputFrequency);
            renderSingleFileStereo(song, voiceExpander, " grp-" + suffix);
        }
    }

    private void renderFilePerInstrument(final Song song, final int outputFrequency) {
        print("Rendering " + song.getName());
        for (int instrumentIndex = 0; instrumentIndex < song.getInstruments().length; instrumentIndex++) {
            if (!song.checkInstrumentIndexInUse(instrumentIndex)) continue;
            song.muteAllInstruments(true);
            SymphonieInstrument instrument = song.getInstrument(instrumentIndex);
            instrument.setMuted(false);
            String suffix = Integer.toString(instrument.getID());
            if (instrumentIndex <= 9) suffix = "00" + suffix;
            if (instrumentIndex > 9 && instrumentIndex <= 99) suffix = "0" + suffix;
            if (instrument.getMultiChannel().equals(MultichannelEnum.StereoL)) {
                SymphonieInstrument instrumentR = song.getInstrument(instrumentIndex + 1);
                instrumentR.setMuted(false);
                instrumentIndex++;
            }
            VoiceExpander voiceExpander = getVoiceExpander(song, outputFrequency);
            renderSingleFileStereo(song, voiceExpander, " I-" + instrument.getRenderDescription() + suffix);
        }
    }

    private void renderFilePerStereoChannel(final Song song, final int outputFrequency) {
        print("Rendering " + song.getName());
        for (int voiceIndex = 0; voiceIndex < song.getNumbOfVoices(); voiceIndex += 2) {
            VoiceExpander voiceExpander = getVoiceExpander(song, outputFrequency);
            voiceExpander.setMuteAllVoices(true);
            voiceExpander.muteVoice(voiceIndex, false);
            voiceExpander.muteVoice(voiceIndex + 1, false);
            String suffix = Integer.toString(voiceIndex);
            if (song.getNumbOfVoices() > 1 && song.getNumbOfVoices() < 99 && voiceIndex <= 9) suffix = "0" + suffix;
            if (song.getNumbOfVoices() > 99) {
                if (voiceIndex <= 9) suffix = "00" + suffix;
                if (voiceIndex <= 99) suffix = "0" + suffix;
            }
            renderSingleFileStereo(song, voiceExpander, " " + suffix + "LR");
        }
    }

    private final TimeMeasure timer = new TimeMeasure();

    private double renderNullStereo(final Song song, final VoiceExpander voiceExpander) {
        logger.debug("renderNullStereo");
        song.muteAllInstruments(false);
        song.PlayFromFirstSequence(false);
        voiceExpander.setSongSpeed(song.getBpm(), song.getPositionSpeed());
        timer.start(RENDER_TIMER_ID);
        int prevPattern = -1;
        while (!voiceExpander.renderToRenderBuffer(null, PRERENDER_BUFFER_LEN) && !shutdown) {
            timer.sum(RENDER_TIMER_ID);
            timer.start(RENDER_TIMER_ID);
            if (Math.abs(song.getPlayingPatternNr() - prevPattern) > 5) {
                System.gc();
                prevPattern = song.getPlayingPatternNr();
                if(song.isPlaying()) print("Time:" + timer.getSumStringShort(RENDER_TIMER_ID) + " " + getVoiceExpander().getRenderInfo());
                notifyUpdate();
            }
        }
        timer.sum(RENDER_TIMER_ID);
        print("Time:" + timer.getSumStringShort(RENDER_TIMER_ID) + " " + getVoiceExpander().getRenderInfo());
        song.stopSong();
        voiceExpander.stopAllVoices();
        logger.debug("renderNullStereo done MaxAmplitude:" + voiceExpander.getMaxAmplitude());
        return voiceExpander.getMaxAmplitude();
    }

    private void renderSingleFileStereo(final Song song, final VoiceExpander voiceExpander, String filesuffix) {
        int samplesRendered = 0;
        logger.debug(voiceExpander.getMuteStatus());
        song.PlayFromFirstSequence(false);
        voiceExpander.setSongSpeed(song.getBpm(), song.getPositionSpeed());
        double[] renderBuffer = new double[RENDER_BUFFER_LEN];
        floatBuffers = new ArrayList<>();
        voiceExpander.setHasSongFinishedPlaying(false);
        timer.start(RENDER_TIMER_ID);
        int prevPattern = -1;
        while (!voiceExpander.renderToRenderBuffer(renderBuffer, RENDER_BUFFER_LEN / 2) && !shutdown) {
            timer.sum(RENDER_TIMER_ID);
            floatBuffers.add(renderBuffer);
            samplesRendered += renderBuffer.length;
            renderBuffer = new double[RENDER_BUFFER_LEN];
            if (song.getPlayingPatternNr() != prevPattern) {
                System.gc();
                prevPattern = song.getPlayingPatternNr();
                notifyUpdate();
            }
            timer.start(RENDER_TIMER_ID);
        }
        timer.sum(RENDER_TIMER_ID);

        if (renderFileFormat.equals(RenderFileFormat.Both) || renderFileFormat.equals(RenderFileFormat.Wav_16_Bit)) {
            collectAudioAndExportWav(floatBuffers, filesuffix, samplesRendered, voiceExpander.getMaxAmplitude() * 100.0 / renderVolume, 16);
        }
        if (renderFileFormat.equals(RenderFileFormat.Both) || renderFileFormat.equals(RenderFileFormat.Flac_24_Bit)) {
            collectAudioAndExportFlac(floatBuffers, filesuffix, samplesRendered, voiceExpander.getMaxAmplitude() * 100.0 / renderVolume, 24);
        }
        song.stopSong();
        voiceExpander.stopAllVoices();
        System.gc();
    }

    private void collectAudioAndExportWav(final List<double[]> inputBuffers
            , final String filesuffix
            , int samplesRendered
            , double maxAplitude
            , final int bitPerSample) {
        try {
            logger.debug("collectAudioAndExportWav");
            int finalSizeinByte = samplesRendered * (bitPerSample / 8); // 16 bit
            if (finalSizeinByte <= 0) return;

            float factor = (float) ((renderVolume / 100.0) / 32767.0f);
            float clipping;
            if (24 == bitPerSample) {
                clipping = 256 * 32767.0f;
                factor *= clipping;
            } else {
                clipping = 32767.0f;
                factor *= clipping;
            }
            byte[] byteBuffer = new byte[samplesRendered * 2];
            if (byteBuffer == null) return;
            int index = 0;
            for (double[] buffer : inputBuffers) {
                for (double sample : buffer) {
                    int intSample = (int) clip(factor * sample, clipping);
                    byteBuffer[index] = (byte) ((intSample >> 8) & 0xff);
                    byteBuffer[index + 1] = (byte) (intSample & 0xff);
                    index += 2;
                }
            }
            logger.debug("collectAudioAndExportWav audio converted");
            if (filepath.toLowerCase().endsWith(".wav")) {
                filepath = filepath.substring(0, filepath.lastIndexOf(".wav"));
            }
            print("Saving " + filepath + filesuffix + ".wav" + "   ("
                    + voiceExpander.getRenderInfo()
                    + ", Calc:" + timer.getSumStringShort(RENDER_TIMER_ID) + ") ");
            File out = new File(filepath + filesuffix + ".wav");
            final boolean bigEndian = true;
            final boolean signed = true;
            final int bits = 16;
            final int channels = 2;
            logger.debug("collectAudioAndExportWav writing file " + byteBuffer + "bytes to " + out.getPath());
            AudioFormat format = new AudioFormat((float) renderFrequency, bits, channels, signed, bigEndian);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer);
            AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, format, byteBuffer.length);
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
            audioInputStream.close();
        } catch (Exception exception) {
            logger.error(exception);
            exception.printStackTrace();
            print("Error:collectAudioAndExportWav" + exception.getMessage());
        }

    }

    private void collectAudioAndExportFlac(final List<double[]> inputBuffers
            , final String filesuffix
            , int samplesRendered
            , double maxAplitude
            , int bitPerSample) {
        try {
            logger.debug("collectAudioAndExportFlac");
            int finalSizeinByte = samplesRendered * (bitPerSample / 8); // 16 bit
            if (finalSizeinByte <= 0) return;


            double factor = (double) ((renderVolume / 100.0) / 32767.0f);
            double clipping;
            if (24 == bitPerSample) {
                clipping = 256 * 32767.0f;
                factor *= clipping;
            } else {
                clipping = 32767.0f;
                factor *= clipping;
            }
            int outIndex = 0;
            int[][] audioBuffer = new int[2][samplesRendered / 2];
            if (audioBuffer == null) {
                logger.error("Error:collectAudioAndExportFlac out of memory");
                return;
            }
            for (double[] buffer : inputBuffers) {
                for (double sample : buffer) {
                    audioBuffer[outIndex % 2][outIndex / 2] = (int) clip(factor * sample, clipping);
                    outIndex++;
                }
            }
            logger.debug("collectAudioAndExportFlac audio converted");
            timer.start("FLAC");
            logger.debug("collectAudioAndExportFlac writing flac file");
            writeFlac(audioBuffer, filesuffix, 24, clipping);
            timer.stop("FLAC");
        } catch (Exception exception) {
            logger.error(exception);
            exception.printStackTrace();
            print("Error:Flac rendering" + exception.getMessage());
        }
    }

    public String getStatistics() {
        StringBuilder text = new StringBuilder();
        text.append("Rendering:" + timer.getSumStringShort(RENDER_TIMER_ID));
        text.append(" Flac encoding:" + timer.getDiffString("FLAC"));
        return text.toString();
    }

    double clip(double sample, double clipping) {
        if (sample > clipping) return clipping;
        if (sample < -clipping) return -clipping;
        return sample;
    }

    private List<double[]> floatBuffers = new ArrayList<>();
    FlacEncoder flacEncoder;
    int oldPercent = 0;

    public void writeFlac(final int[][] samples, final String filesuffix, int bitPerSample, double clipping) throws IOException {
        oldPercent = 0;
        if (filepath.toLowerCase().endsWith(".wav")) {
            filepath = filepath.substring(0, filepath.lastIndexOf(".wav"));
        }
        print("Saving " + filepath + filesuffix + ".flac" + "   ("
                + voiceExpander.getRenderInfo() + ", "
                + timer.getSumStringShort(RENDER_TIMER_ID) + ") ");
        File out = new File(filepath + filesuffix + ".flac");
        logger.debug("writeFlac " + out.getPath());
        try (RandomAccessFile outputFile = new RandomAccessFile(out, "rw")) {
            outputFile.setLength(0);  // Truncate an existing file
            BitOutputStream bitOutputStream = new BitOutputStream(
                    new BufferedOutputStream(new RandomAccessFileOutputStream(outputFile)));
            bitOutputStream.writeInt(32, 0x664C6143);

            // Populate and write the stream info structure
            StreamInfo info = new StreamInfo();
            info.sampleRate = renderFrequency;
            info.numChannels = samples.length;
            info.sampleDepth = bitPerSample;
            info.numSamples = samples[0].length;
            info.md5Hash = StreamInfo.getMd5Hash(samples, info.sampleDepth);
            info.write(true, bitOutputStream);
            flacEncoder = new FlacEncoder(info, samples, 4096, SubframeEncoder.SearchOptions.SUBSET_BEST, bitOutputStream) {
                @Override
                public void notifyUI(double percentFinished) {
                    if (oldPercent == (int) percentFinished) return;
                    oldPercent = (int) percentFinished;
                    print("Writing Flac " + oldPercent + "%");
                    notifyUpdate();
                }
            };
            bitOutputStream.flush();
            outputFile.seek(4);
            info.write(true, bitOutputStream);
            bitOutputStream.flush();
        }
        logger.debug("writeFlac done:" + out.getPath());
    }

    private List<String> messages = new Vector<>();

    private void print(final String text) {
        messages.add(text);
    }

    public List<String> getAllMessages() {
        List<String> tempMessages = messages;
        messages = new Vector<>();
        return tempMessages;
    }

    public String getLatesMessage() {
        List<String> tempMessages = messages;
        messages = new Vector<>();
        if (tempMessages.isEmpty()) return "";
        return tempMessages.get(tempMessages.size() - 1);
    }
}
