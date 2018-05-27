package com.luis.cortes.vehiclediagnostics;

import android.util.Log;

public class VehStats {
    public interface Formula {
        public double calculate(int a, int b, int c, int d);
    }

    static double getValue(Response resp, Formula formula) {
        int A = resp.getA();
        int B = resp.getB();
        int C = resp.getC();
        int D = resp.getD();

        return formula.calculate(A, B, C, D);
    }
}
