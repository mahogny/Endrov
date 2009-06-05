package logobar;


public class Preferences{

    
    
    public boolean iIsBlockDivided;
    public boolean iShowOnlyMostConserved;
    public int     iFontSize;
    public int     iBlockSize;
    public boolean iIsLettersAtBottom;
    public boolean iShowLetterGraph;
    public boolean iUseCorrectionFactor;
    public int     iFreqCutoff;
    public boolean iShowWebLogo;

    public Preferences(){

	iIsBlockDivided = false;
	iShowOnlyMostConserved = false;
	iIsLettersAtBottom = true;
	iShowLetterGraph   = true;
	iFontSize = 16;
	iBlockSize = 40;
	iUseCorrectionFactor = true;
	iFreqCutoff = 5;
	iShowWebLogo = false;
	
    }
    

}
