package ch.meng.symphoniefx;

import javax.sound.midi.*;
import java.util.List;
import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;


public class MidiManager {
    MidiDevice device;
    MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

    public MidiManager() {
        for (int i = 0; i < infos.length; i++) {
            try {
                //device = MidiSystem.getMidiDevice(infos[i]);
//                System.out.println(infos[i]);
//                List<Transmitter> transmitters = device.getTransmitters();
//                for (int j = 0; j < transmitters.size(); j++) {
//                    transmitters.get(j).setReceiver(new MidiInputReceiver(device.getDeviceInfo().toString()));
//                }
//                Transmitter trans = device.getTransmitter();
//                trans.setReceiver(new MidiInputReceiver(device.getDeviceInfo().toString()));
                //device.open();
                System.out.println(device.getDeviceInfo() + " Was Opened");
            } catch (Exception e) {
            }
        }
    }

    void shutdown() {
        for (int i = 0; i < infos.length; i++) {
            try {
                device = MidiSystem.getMidiDevice(infos[i]);
                device.close();
                System.out.println(device.getDeviceInfo() + " Was Opened");
            } catch (MidiUnavailableException e) {
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            shutdown();
        } finally {
            super.finalize();
        }
    }

    //tried to write my own class. I thought the send method handles an MidiEvents sent to it
    public class MidiInputReceiver implements Receiver {
        public String name;
        public MidiInputReceiver(String name) {
            this.name = name;
        }

        public void send(MidiMessage msg, long timeStamp) {
            System.out.println("midi received");
        }

        public void close() {
        }
    }


}

