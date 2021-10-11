/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.meng.symphoniefx.dsp;

import symreader.FilterType;

public class DspMonoFilterSymphonie implements DspMonoFx {
    double lowpass, mem2, mem3; // samplememory
    double frequency; // filterfreq
    double resonance;
    private FilterType filtertype = FilterType.Off;
    private double audioDeviceFrequency = 44100;

    public  DspMonoFilterSymphonie() {
        init(FilterType.Off, 0, 0, 44100);
        clearSampleBuffers();
    }

    private void clearSampleBuffers() {
        lowpass = 0;
        mem2 = 0;
        mem3 = 0;
    }

    @Override
    public void adjust(double qFactor, double filterFreq, final double audioDeviceFrequency) {
        this.audioDeviceFrequency = audioDeviceFrequency;
        this.frequency = filterFreq;
        this.resonance = qFactor;
        if(this.frequency>255) this.frequency = 255;
        if(this.resonance>192) this.resonance = 192;
        if(this.frequency<0) this.frequency = 0;
        if(this.resonance<0) this.resonance = 0;
    }

    @Override
    public void init(final FilterType filterType, double qFactor, double filterFreq, final double audioDeviceFrequency) {
        clearSampleBuffers();

        this.filtertype = filterType;
        if (filterFreq == 0.0) {
            filtertype = FilterType.Off;
            return;
        }
        adjust(qFactor, filterFreq, audioDeviceFrequency);
//        this.filterFreq = 2 * Math.sin(Math.PI * filterFreq * 10 / audioDeviceFrequency);
//        //freq = (f*1.25f/100.0f);
//        if (myq != 0) {
//            recq = 1 / myq;
//        } else {
//            recq = 1;
//        }
    }

    @Override
    public boolean isActive() {
        return filtertype != FilterType.Off;
    }

    @Override
    public double stream(double sample) {
        if (filtertype == FilterType.Off) return sample;
        mem3 = clip(sample - lowpass);
        mem2 = clip(mem2 + (mem3 * frequency / 256));
        lowpass = clip((lowpass + (mem2 * frequency / 64) + (lowpass * resonance / 64)) / 4);
        return lowpass;
    }

    double max = 1.0;
    public void setClipping(double max) {
        this.max = max;
    }

    private double clip(double Sample) {
        if (Sample > max) return max;
        if (Sample < -max) return -max;
        return Sample;
    }

    public String getShortDescription() {
        if(!this.isActive()) return "";
        StringBuilder text = new StringBuilder();
        text.append("Reso:").append((int) this.resonance)
                .append("Freq:").append((int) this.frequency);
        return text.toString();
    }
//    ;LP Filter original source
//    RTResoFilterPostMixLP
//    puts	d0-d7/a1-a2
//    subq.w	#1,d0
//    movem.l	RTFILTER_FILTERBUF(a0),d2-d4
//    move.l	RTFILTER_FILTERFREQ(a0),d7
//    cmpi.l	#CFILTER_MAXFREQ,d7
//    bls.s	.freqOK
//    move.l	#CFILTER_MAXFREQ,d7
//.freqOK	tst.l	RTFILTER_FILTERRESO(a0)
//    beq.s	.donoreso
//    cmpi.l	#CFILTER_MAXRESO,RTFILTER_FILTERRESO(a0)
//    bls.s	.loop
//    move.l	#CFILTER_MAXRESO,RTFILTER_FILTERRESO(a0)
//.loop	move.w	(a2),d1
//            ;--- FILTER MIT RESO ---
//    ext.l	d1
//    move.l	d1,d4
//    sub.l	d2,d4
//    move.l	d7,d6
//    muls.l	d4,d6
//    asr.l	#8,d6
//    add.l	d6,d3
//    move.l	d7,d6
//    muls.l	d3,d6
//    asr.l	#6,d6
//    add.l	d6,d2
//    move.l	RTFILTER_FILTERRESO(a0),d6
//    muls.l	d2,d6
//    asr.l	#6,d6
//    add.l	d6,d2
//    asr.l	#2,d2
//    move.l	d2,d1
//            ;-----------------------
//    add.w	d1,(a1)
//            clr.w	(a2)
//    lea.l	4(a2),a2
//    dbf	d0,.loop
//.exit	movem.l	d2-d4,RTFILTER_FILTERBUF(a0)
//    gets	d0-d7/a1-a2
//            rts
}
