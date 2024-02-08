package ch.meng.symphoniefx.mixer;

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
            if (!isSpeaker(mixerinfo) || isCapture(mixerinfo)) continue;
            javaDevices.add(mixerinfo);
            javaDrivers.add(mixerinfo.getName() + ":" + mixerinfo.getDescription());
        }
        for (Mixer.Info mixerinfo : AudioSystem.getMixerInfo()) {
            if (isSpeaker(mixerinfo) || isCapture(mixerinfo)) continue;
            javaDevices.add(mixerinfo);
            javaDrivers.add(mixerinfo.getName() + ":" + mixerinfo.getDescription());
        }
        List<String> temp = new Vector<>();
        temp.addAll(javaDrivers);
        return temp;
    }

    private boolean isSpeaker(Mixer.Info mixerinfo) {
        return mixerinfo.getDescription().toLowerCase().contains("playback")
                || mixerinfo.getDescription().toLowerCase().contains("speaker");
    }

    private boolean isCapture(Mixer.Info mixerinfo) {
        return mixerinfo.getDescription().toLowerCase().contains("capture");
    }

    public Mixer.Info getMixerInfo(int index) {
        if(index < 0) index = 0;
        return javaDevices.get(index);
    }

    public JavaAudioDevice getSpecificAudioDevice(String audioDeviceName, int numberOfHardwareChannels, int samplesPerChannel, int mixFrequency, double oversample) {
        int index = 0;
        for(Mixer.Info device : javaDevices) {
            if(device.getName().equals(audioDeviceName)) {
                index = javaDevices.indexOf(device);
                break;
            }
        }
        JavaAudioDevice javaAudioDevice = new JavaAudioDevice();
        javaAudioDevice.init(javaDevices.get(index), numberOfHardwareChannels, samplesPerChannel, mixFrequency, oversample);
        return javaAudioDevice;
    }

    public JavaAudioDevice getDefaultAudioDevice(int numberOfHardwareChannels, int samplesPerChannel, int mixFrequency, double oversample) {
        JavaAudioDevice javaAudioDevice = new JavaAudioDevice();
        javaAudioDevice.init(javaDevices.get(0), numberOfHardwareChannels, samplesPerChannel, mixFrequency, oversample);
        return javaAudioDevice;
    }
}
