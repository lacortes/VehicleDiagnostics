package com.luis.cortes.vehiclediagnostics;

import android.util.Log;

public class VehStats {
    static double getValue(Response resp, Formula formula) {
        int A = resp.getA();
        int B = resp.getB();
        int C = resp.getC();
        int D = resp.getD();

        return formula.calculate(A, B, C, D);
    }
}
