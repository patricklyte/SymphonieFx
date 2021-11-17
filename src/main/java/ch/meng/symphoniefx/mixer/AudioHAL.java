package ch.meng.symphoniefx.mixer;

import ch.meng.symphoniefx.mixer.JavaAudioDevice;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.List;
import java.util.Vector;

public class AudioHAL {
    private List<String> asioDrivers = new Vector<>();
    private List<String> javaDrivers = new Vector<>();
    private List<Mixer.Info> javaDevices = new Vector<>();

    public List<String> getAudioOutputDevices() {
        javaDevices.clear();
        javaDrivers.clear();
        for (Mixer.Info mixerinfo : AudioSystem.getMixerInfo()) {
            if (!mixerinfo.getDescription().toLowerCase().contains("playback")
                    && !mixerinfo.getDescription().toLowerCase().contains("speaker")
            ) continue;
            javaDevices.add(mixerinfo);
            javaDrivers.add(mixerinfo.getName() + ":" + mixerinfo.getDescription());
        }
        for (Mixer.Info mixerinfo : AudioSystem.getMixerInfo()) {
            javaDevices.add(mixerinfo);
            javaDrivers.add(mixerinfo.getName() + ":" + mixerinfo.getDescription());
        }
        List<String> temp = new Vector<>();
        temp.addAll(javaDrivers);
        return temp;
    }

    public Mixer.Info getMixerInfo(int index) {
        return javaDevices.get(index);
    }

//    public JavaAudioDevice getAudioDevice(int index) {
//        JavaAudioDevice javaAudioDevice = new JavaAudioDevice();
//        javaAudioDevice.init(javaDevices.get(index), 64, 512, 44100, 1);
//        return javaAudioDevice;
//    }

    public JavaAudioDevice getDefaultAudioDevice(int numberOfHardwareChannels, int samplesPerChannel, int mixFrequency, double oversample) {
        JavaAudioDevice javaAudioDevice = new JavaAudioDevice();
        javaAudioDevice.init(javaDevices.get(0), numberOfHardwareChannels, samplesPerChannel, mixFrequency, oversample);
        return javaAudioDevice;
    }
}
