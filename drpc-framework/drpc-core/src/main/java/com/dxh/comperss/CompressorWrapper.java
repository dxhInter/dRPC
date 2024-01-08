package com.dxh.comperss;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompressorWrapper {
    private byte code;
    private String type;
    private Compressor compressor;
}
