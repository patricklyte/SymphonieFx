package ch.meng.symphoniefx.rendering;

public enum RenderFileFormat {
    Flac_24_Bit("Flac 24 Bit"),
    Wav_16_Bit("Wav 16 Bit"),
    Both("Both");

    RenderFileFormat(String text) {
    }

    @Override
    public String toString() {
        return this.name();
    }
}
