/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.meng.symphoniefx.dsp;

public class DSP3BandEQ {
    private double lpFrequency;       // Frequency
    private double f1p0;     // Poles ...
    private double f1p1;
    private double f1p2;
    private double f1p3;

    // Filter #2 (High band)

    private double hpFrequency;       // Frequency
    private double f2p0;     // Poles ...
    private double f2p1;
    private double f2p2;
    private double f2p3;

    // Sample history buffer

    private double sdm1;     // Sample data minus 1
    private double sdm2;     //                   2
    private double sdm3;     //                   3

    // Gain Controls
    private double lowGain;       // low  gain
    private double middleGain;       // mid  gain
    private double highGain;       // high gain
    private boolean active = false;
    private static final double vsa = (1.0 / 4294967295.0);   // Very small amount (Denormal Fix)
    private double mixfrequency = 44100;

    public DSP3BandEQ() {
        init(false, 880, 5000, 44100);
        clear();
    }

    public double getLowGain() {
        return lowGain;
    }

    public double getMiddleGain() {
        return middleGain;
    }

    public double getHighGain() {
        return highGain;
    }

    public boolean isActive() {
        return (active);
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public void clear() {
        sdm1 = 0;
        sdm2 = 0;
        sdm3 = 0;
    }

    public void setFilterTabs(double lowGain, double middleGain, double highGain) {
        setLowGain(lowGain);
        setMidGain(middleGain);
        setHighGain(highGain);
    }

    public void setLowGain(double gain) {
        lowGain = 1.0 + gain;
    }

    public void setMidGain(double gain) {
        middleGain = 1.0 + gain;
    }

    public void setHighGain(double gain) {
        highGain = 1.0 + gain;
    }

    public void init(boolean activate) //
    {
        active = activate;
    }

    public void init(boolean activate, int lpFrequency, int hpFrequency, int mymixfreq) //
    {
        // Set Low/Mid/High gains to unity
        lowGain = 1.0;
        middleGain = 1.0;
        highGain = 1.0;

        if (mymixfreq == 0) mymixfreq = 44000;
        mixfrequency = mymixfreq;

        // Calculate filter cutoff frequencies
        setLPFrequency(lpFrequency);
        setHPFrequency(hpFrequency);
        active = activate;
    }

    public void setLPFrequency(double frequency) {
        lpFrequency = 2 * java.lang.Math.sin(java.lang.Math.PI * frequency / mixfrequency);
    }

    public double getLPFrequency() {
        return lpFrequency;
    }

    public double getHPFrequency() {
        return hpFrequency;
    }

    public void setHPFrequency(double frequency) {
        hpFrequency = 2 * java.lang.Math.sin(java.lang.Math.PI * frequency / mixfrequency);
    }

    public double stream(final double sample) {
        if(!active) return sample;
        double l, m, h;      // Low / Mid / High - Sample Values
        // Filter #1 (lowpass)

        f1p0 += (lpFrequency * (sample - f1p0)) + vsa;
        f1p1 += (lpFrequency * (f1p0 - f1p1));
        f1p2 += (lpFrequency * (f1p1 - f1p2));
        f1p3 += (lpFrequency * (f1p2 - f1p3));

        l = f1p3;

        // Filter #2 (highpass)
        f2p0 += (hpFrequency * (sample - f2p0)) + vsa;
        f2p1 += (hpFrequency * (f2p0 - f2p1));
        f2p2 += (hpFrequency * (f2p1 - f2p2));
        f2p3 += (hpFrequency * (f2p2 - f2p3));

        h = sdm3 - f2p3;

        // Calculate midrange (signal - (low + high))
        m = sdm3 - (h + l);

        // Scale, Combine and store
        l *= lowGain;
        m *= middleGain;
        h *= highGain;

        // Shuffle history buffer
        sdm3 = sdm2;
        sdm2 = sdm1;
        sdm1 = sample;

        // Return result
        return l + m + h;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        if(active) {
            text.append("EQ:");
        } else {
            text.append("EQ Inactive:");
        }
        text.append(" low ").append(100 * lowGain).append("%")
                .append(" mid ").append(100 * middleGain).append("%")
                .append(" high ").append(100 * highGain).append("%");
        return text.toString();
    }
}


//
//3 Band Equaliser
//
//References : Posted by Neil C
//
//Notes : 
//Simple 3 band equaliser with adjustable low and high frequencies ...
//
//Fairly fast algo, good quality output (seems to be accoustically transparent with all gains set to 1.0)
//
//How to use ...
//
//1. First you need to declare a state for your eq
//
//EQSTATE eq;
//
//2. Now initialise the state (we'll assume your output frequency is 48Khz)
//
//set_3band_state(eq,880,5000,480000);
//
//Your EQ bands are now as follows (approximatley!)
//
//low band = 0Hz to 880Hz
//mid band = 880Hz to 5000Hz
//high band = 5000Hz to 24000Hz
//
//3. Set the gains to some values ...
//
//eq.lg = 1.5; // Boost bass by 50%
//eq.mg = 0.75; // Cut mid by 25%
//eq.hg = 1.0; // Leave high band alone 
//
//4. You can now EQ some samples
//
//out_sample = do_3band(eq,in_sample)


// ---------------
//| Initialise EQ |
// ---------------

// Recommended frequencies are ...
//
//  lowfreq  = 880  Hz
//  highfreq = 5000 Hz
//
// Set mixfreq to whatever rate your system is using (eg 48Khz)


// ---------------
//| EQ one sample |
// ---------------

// - sample can be any range you like :)
//
// Note that the output will depend on the gain settings for each band 
// (especially the bass) so may require clipping before output, but you 
// knew that anyway :)

