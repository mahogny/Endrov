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
package endrov.utilityUnsorted.deconvolution.iterative.mrnsd;

/**
 * MRNSD options.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class MRNSDOptions {

    private boolean autoStoppingTol;
    private double stoppingTol;
    private boolean useThreshold;
    private double threshold;
    private boolean logConvergence;

    public MRNSDOptions() {
        this(true, 0, false, 0, false);
    }

    public MRNSDOptions(boolean autoStoppingTol, double stoppingTol, boolean useThreshold, double threshold, boolean logConvergence) {
        this.autoStoppingTol = autoStoppingTol;
        this.stoppingTol = stoppingTol;
        this.useThreshold = useThreshold;
        this.threshold = threshold;
        this.logConvergence = logConvergence;
    }

    public boolean getAutoStoppingTol() {
        return autoStoppingTol;
    }

    public void setAutoStoppingTol(boolean autoStoppingTol) {
        this.autoStoppingTol = autoStoppingTol;
    }

    public double getStoppingTol() {
        return stoppingTol;
    }

    public void setStoppingTol(double stoppingTol) {
        this.stoppingTol = stoppingTol;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public boolean getUseThreshold() {
        return useThreshold;
    }

    public void setUseThreshold(boolean useThreshold) {
        this.useThreshold = useThreshold;
    }

    public boolean getLogConvergence() {
        return logConvergence;
    }

    public void setLogConvergence(boolean logConvergence) {
        this.logConvergence = logConvergence;
    }

}
