package ch.meng.symphoniefx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.midi.*;

public class MidiManager {
    private static final Logger logger = LogManager.getLogger();
    private MidiDevice.Info[] infos;
    private Receiver receiver;
    private MidiDevice midiDevice;
    private Transmitter transmitter;

    void onKeyOn(int pitch, int punch) {

    }
    void onKeyOff(int pitch) {

    }
    void onController(int command, int data1, int data2) {}

    public MidiManager() {
        try {
            infos = MidiSystem.getMidiDeviceInfo();
            MidiDevice.Info keyboardInfo = null;
            for (MidiDevice.Info deviceInfo : infos) {
                logger.debug(deviceInfo.getName());
                midiDevice = MidiSystem.getMidiDevice(deviceInfo);
                if (deviceInfo.getName().contains("Axiom 49 MIDI In")) {
                    keyboardInfo = deviceInfo;
                    //break;
                }
            }
            if(null==keyboardInfo) {
                logger.debug("Midi Device not found:Axiom 49 MIDI In");
                return;
            }
            midiDevice = MidiSystem.getMidiDevice(keyboardInfo);
            midiDevice.open();
            transmitter = midiDevice.getTransmitter();
            receiver = new MidiInputReceiver(keyboardInfo.getName());
            transmitter.setReceiver(receiver);
        } catch (Exception e) {
            logger.debug(e);
        }
    }

    void shutdown() {
        if (receiver != null) {
            receiver.close();
            receiver = null;
        }
        if (transmitter != null) {
            transmitter.close();
            transmitter = null;
        }
        if (midiDevice != null) {
            midiDevice.close();
            midiDevice = null;
        }


        for (MidiDevice.Info info : infos) {
            try {
                var device = MidiSystem.getMidiDevice(info);
                device.close();
                System.out.println(device.getDeviceInfo() + " Was Opened");
            } catch (MidiUnavailableException ignored) {
            }
        }
    }

    public class MidiInputReceiver implements Receiver {
        public String name;

        public MidiInputReceiver(String name) {
            this.name = name;
        }

        public void send(MidiMessage msg, long timeStamp) {
            //var terts = msg.getMessage();

            if(msg instanceof ShortMessage shortMessage) {
                var data1 = shortMessage.getData1();
                var data2 = shortMessage.getData2();
                var comand = shortMessage.getCommand();
                //logger.debug("midi:"+ data1 + " " + data2 + " "+ comand);
                if(shortMessage.getCommand() == ShortMessage.NOTE_ON) {
                    if(data2 == 0) {
                        logger.debug("KEY_OFF pitch " + data1 + " punch "+ data2);
                        onKeyOff(data1);
                    } else {
                        logger.debug("KEY_ON pitch " + data1 + " punch "+ data2);
                        onKeyOn(data1, data2);
                    }
                }
                else if(shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
                    logger.debug("midi NOTE_OFF received" + msg);
                } else {
                    onController(shortMessage.getCommand(), shortMessage.getData1(), shortMessage.getData2());
                }
            }
            else {
                logger.debug("midi received" + msg);
            }

            logger.debug("midi received" + msg);
        }

        public void close() {
        }
    }


}

