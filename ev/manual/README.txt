Virtual WormBase VWB
##################################################################################

License
--------------------------------------------------------------------
This software falls under the BSD3 license. See LICENSE.txt

What is VWB?
--------------------------------------------------------------------
Virtual Worm Base is an application written at Karolinska Institute, primary for studying C.Elegans.

User Guide
--------------------------------------------------------------------
For the sake of keeping things up to date, refer to our website for any documentation.
URL here.

Contact
--------------------------------------------------------------------
Primary maintainer:
  Johan Henriksson
  Karolinska Institutet/Sšdertšrns hšgskola
  johan.henriksson@biosci.ki.se












Terminology
--------------------------------------------------------------------
recording
database???
stack
slice
channel
marker
microscope time
worm time


Console Commands !!!!OUTDATED!!!!
--------------------------------------------------------------------
(ren | rename) from to
  Change <from> to <to> in all nuclei names. Wildcards are NOT supported (yet).
(ren | rename) to
  As before, but the cell to rename is supposed to be selected.
(rc | renameclass) from to
(rc | renameclass)
  Like rename, put will append a number in such a way that the name becomes unique.
rcc
  Like rc/rename, but accepts several nuclei in selection list

swap cell1 cell2
  Swap the names of two nuclei

sellist
  Gives name of all select nucleis

quit
  Exits VWB

view back | front...
  Shows a preset view in the last selected image window

sel <nucleiname> ...
unsel <nucleiname> ...
  Select/Unselect nucleis. No arguments=*

show <nucleiname> ...
hide <nucleiname> ...
  Show/Hide nucleis. No arguments=*

wb (<nucleiname>)
  Opens upp the given cell in wormbase. If name not given, opens for selected cells

(j | jump) f<framenum> | m<microscope time in sec> | w<worm time in sec> | l1 | l2 etc | nucleiname
  Jump to a certain time based either on absolute time or by a stage in development

grid
  Toggles grid on/off

(cellnames | cnames)
  Toggles Show All Cellnames on/off

delnuc <nuc>
  Removes a nucleus
delframe
  Removes current key frame


lin
  Open lineage window
3d
  Open 3D-window
im
  open imagewindow

help
  Online help


2D Image Controls   !!!!OUTDATED!!!
--------------------------------------------------------------------
W  Go up in sample
S  Go down in sample
D  Next frame
A  Previous frame

!!!!
Z+move mouse
   Translate nuclei
C+move mouse horizontal
   Rescale nuclei
X
   Set nucleus Z to current layer
E
   Split nucleus
!!!!?

Esc/Backspace
   Move focus to console


3D Viewer Controls
--------------------------------------------------------------------
Esc/Backspace
   Move focus to console

G  Toggle grid
N  Toggle cellnames






Name matching !!!OUTDATED!!!
--------------------------------------------------------------------
Several commands allow the usage of wildcards (*). On GNU platforms, ksh patterns are also supported.
The patterns are written in the form explained in the following table where pattern-list is a | separated list of patterns.

?(pattern-list)
  The pattern matches if zero or one occurrences of any of the patterns in the pattern-list allow matching the input string. 

*(pattern-list)
  The pattern matches if zero or more occurrences of any of the patterns in the pattern-list allow matching the input string. 

+(pattern-list)
  The pattern matches if one or more occurrences of any of the patterns in the pattern-list allow matching the input string. 

@(pattern-list)
  The pattern matches if exactly one occurrence of any of the patterns in the pattern-list allows matching the input string. 

!(pattern-list)
  The pattern matches if the input string cannot be matched with any of the patterns in the pattern-list.




