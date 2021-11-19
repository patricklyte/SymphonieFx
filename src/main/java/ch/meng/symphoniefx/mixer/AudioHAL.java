package ch.meng.symphoniefx.mixer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class AudioHAL {
    private static final Logger logger = LogManager.getLogger();

    private List<String> asioDrivers = new Vector<>();
    private List<String> javaDrivers = new Vector<>();
    private List<Mixer.Info> javaDevices = new Vector<>();

    public List<String> getAudioOutputDevices() {
        javaDevices.clear();
        javaDrivers.clear();
        Line.Info info = new Line.Info(TargetDataLine.class);
        for(Line.Info lineInfo : AudioSystem.getTargetLineInfo(info)) {
            logger.debug(lineInfo);
        }
        Mixer defaultMixer = AudioSystem.getMixer(null);
        addAudioDevice(defaultMixer.getMixerInfo(), " (DEFAULT)");
        Arrays.stream(AudioSystem.getMixerInfo()).filter(this::isOutputAudioDevice).forEach(this::addAudioDevice);
        List<String> temp = new Vector<>();
        temp.addAll(javaDrivers);
        return temp;
    }

    private void addAudioDevice(Mixer.Info mixerInfo, final String postfix) {
        javaDevices.add(mixerInfo);
        javaDrivers.add(mixerInfo + ":" + mixerInfo.getDescription() + postfix);
    }

    private void addAudioDevice(Mixer.Info mixerInfo) {
        addAudioDevice(mixerInfo, "");
    }

    private boolean isOutputAudioDevice(Mixer.Info mixerInfo) {
        if (mixerInfo.getDescription().toLowerCase().contains("playback")
                || mixerInfo.getDescription().toLowerCase().contains("speaker")) return true;
        Mixer mixer = AudioSystem.getMixer(mixerInfo);
        return mixer.getSourceLineInfo().length == 0;
    }

    public Mixer.Info getMixerInfo(int index) {
        return javaDevices.get(index);
    }

    public JavaAudioDevice getDefaultAudioDevice(int numberOfHardwareChannels, int samplesPerChannel, int mixFrequency, double oversample) {
        JavaAudioDevice javaAudioDevice = new JavaAudioDevice();
        javaAudioDevice.init(javaDevices.get(0), numberOfHardwareChannels, samplesPerChannel, mixFrequency, oversample);
        return javaAudioDevice;
    }
}
