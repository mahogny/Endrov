on run (argv)
	set imgseqloc to item 1 of argv
	set outputname to item 2 of argv
	with timeout of 3600 seconds
		
		--set targetfile to choose file with prompt "Where is the image sequence"
		set targetfile to (POSIX file imgseqloc) as string
		set outputname to (POSIX file outputname) as string
		
		tell application "QuickTime Player"
			set mymovie to open image sequence targetfile seconds per frame 0.1
		end tell
		
		set oldcb to "0 0"
		--set newcb to askvalues(oldcb)
		set contr to 0
		set brightn to 0
		-- setcontrbrightn(contr, brightn, mymovie) -- already done by imagemagick
		
		tell application "QuickTime Player"
			--set outputfile to choose file name with prompt "filename for movie?"
			set outputfile to outputname
			export mymovie to outputfile as QuickTime movie
			close mymovie saving no
		end tell
	end timeout
end run