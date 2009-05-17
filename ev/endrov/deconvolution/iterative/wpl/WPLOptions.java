/*  License:
 Copyright (c) 2005, OptiNav, Inc.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 Neither the name of OptiNav, Inc. nor the names of its contributors
 may be used to endorse or promote products derived from this software
 without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package endrov.deconvolution.iterative.wpl;

/**
 * Options for WPL.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class WPLOptions {

    private double gamma;
    private double filterXY;
    private double filterZ;
    private boolean normalize;
    private boolean logConvergence;
    private boolean antiRing;
    private double changeThreshPercent;
    private boolean dB;
    private boolean detectDivergence;
    private boolean useThreshold;
    private double threshold;

    /**
     * Creates new instance of WPLOptions with default parameters.
     * 
     */
    public WPLOptions() {
        this(0, 1.0, 1.0, false, false, true, 0.01, false, true, false, 0);
    }

    /**
     * Creates new instance of WPLOptions.
     * 
     */
    public WPLOptions(double gamma, double filterXY, double filterZ, boolean normalize, boolean logConvergence, boolean antiRing, double changeThreshPercent, boolean dB, boolean detectDivergence, boolean useThreshold, double threshold) {
        this.gamma = gamma;
        this.filterXY = filterXY;
        this.filterZ = filterZ;
        this.normalize = normalize;
        this.logConvergence = logConvergence;
        this.antiRing = antiRing;
        this.changeThreshPercent = changeThreshPercent;
        this.dB = dB;
        this.detectDivergence = detectDivergence;
        this.useThreshold = useThreshold;
        this.threshold = threshold;
    }

    public double getChangeThreshPercent() {
        return changeThreshPercent;
    }

    public double getFilterXY() {
        return filterXY;
    }

    public double getFilterZ() {
        return filterZ;
    }

    public double getGamma() {
        return gamma;
    }

    public double getThreshold() {
        return threshold;
    }

    public boolean isAntiRing() {
        return antiRing;
    }

    public boolean isDB() {
        return dB;
    }

    public boolean isDetectDivergence() {
        return detectDivergence;
    }

    public boolean isLogConvergence() {
        return logConvergence;
    }

    public boolean isNormalize() {
        return normalize;
    }

    public boolean isUseThreshold() {
        return useThreshold;
    }

    public void setAntiRing(boolean antiRing) {
        this.antiRing = antiRing;
    }

    public void setChangeThreshPercent(double changeThreshPercent) {
        this.changeThreshPercent = changeThreshPercent;
    }

    public void setDB(boolean db) {
        dB = db;
    }

    public void setDetectDivergence(boolean detectDivergence) {
        this.detectDivergence = detectDivergence;
    }

    public void setFilterXY(double filterXY) {
        this.filterXY = filterXY;
    }

    public void setFilterZ(double filterZ) {
        this.filterZ = filterZ;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public void setLogConvergence(boolean logConvergence) {
        this.logConvergence = logConvergence;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setUseThreshold(boolean useThreshold) {
        this.useThreshold = useThreshold;
    }
}
