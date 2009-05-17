/*
 *  Copyright (C) 2009 Piotr Wendykier, Johan Henriksson
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package endrov.deconvolution.iterative.hybr;

import cern.colt.matrix.tdouble.algo.solver.HyBRInnerSolver;
import cern.colt.matrix.tdouble.algo.solver.HyBRRegularizationMethod;

/**
 * HyBR options.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class HyBROptions {
    private HyBRInnerSolver innerSolver;
    private HyBRRegularizationMethod regMethod;
    private double regParameter;
    private double omega;
    private boolean reorthogonalize;
    private int beginReg;
    private double flatTolerance;
    private boolean logConvergence;
    private boolean useThreshold;
    private double threshold;

    public HyBROptions() {
        this(HyBRInnerSolver.TIKHONOV, HyBRRegularizationMethod.ADAPTWGCV, 0, 0, false, 2, 1e-6, false, true, 0);
    }

    public HyBROptions(HyBRInnerSolver innerSolver, HyBRRegularizationMethod regMethod, double regParameter, double omega, boolean reorthogonalize, int beginReg, double flatTolerance, boolean computeRnrm, boolean useThreshold, double threshold) {
        this.innerSolver = innerSolver;
        this.regMethod = regMethod;
        this.regParameter = regParameter;
        this.omega = omega;
        this.reorthogonalize = reorthogonalize;
        this.beginReg = beginReg;
        this.flatTolerance = flatTolerance;
        this.logConvergence = computeRnrm;
        this.useThreshold = useThreshold;
        this.threshold = threshold;
    }

    public int getBeginReg() {
        return beginReg;
    }

    public void setBeginReg(int beginReg) {
        this.beginReg = beginReg;
    }

    public double getFlatTolerance() {
        return flatTolerance;
    }

    public void setFlatTolerance(double flatTolerance) {
        this.flatTolerance = flatTolerance;
    }

    public HyBRInnerSolver getInnerSolver() {
        return innerSolver;
    }

    public void setInnerSolver(HyBRInnerSolver innerSolver) {
        this.innerSolver = innerSolver;
    }

    public double getOmega() {
        return omega;
    }

    public void setOmega(double omega) {
        this.omega = omega;
    }

    public HyBRRegularizationMethod getRegMethod() {
        return regMethod;
    }

    public void setRegMethod(HyBRRegularizationMethod regMethod) {
        this.regMethod = regMethod;
    }

    public double getRegParameter() {
        return regParameter;
    }

    public void setRegParameter(double regParameter) {
        this.regParameter = regParameter;
    }

    public double getThreshold() {
        return threshold;
    }

    public boolean getUseThreshold() {
        return useThreshold;
    }

    public void setUseThreashold(boolean useThreshold) {
        this.useThreshold = useThreshold;
    }

    public void setThreashold(double threshold) {
        this.threshold = threshold;
    }

    public boolean getReorthogonalize() {
        return reorthogonalize;
    }

    public void setReorthogonalize(boolean reorthogonalize) {
        this.reorthogonalize = reorthogonalize;
    }

    public boolean getLogConvergence() {
        return logConvergence;
    }

    public void setLogConvergence(boolean logConvergence) {
        this.logConvergence = logConvergence;
    }

}
