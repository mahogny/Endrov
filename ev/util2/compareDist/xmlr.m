clear

infoLabel = 'Plot Tools';
infoCbk = '';
itemFound = false;

xDoc = xmlread(fullfile(matlabroot, ...
               'toolbox/matlab/general/info.xml'));

% Find a deep list of all listitem elements.
allListItems = xDoc.getElementsByTagName('listitem');

% Note that the item list index is zero-based.
for k = 0:allListItems.getLength-1
   thisListItem = allListItems.item(k);
   childNode = thisListItem.getFirstChild;
 
   while ~isempty(childNode)
      %Filter out text, comments, and processing instructions.
      if childNode.getNodeType == childNode.ELEMENT_NODE
         % Assume that each element has a single
         % org.w3c.dom.Text child.
         childText = char(childNode.getFirstChild.getData);

         switch char(childNode.getTagName)
         case 'label';
            itemFound = strcmp(childText, infoLabel);
         case 'callback' ;
            infoCbk = childText;
         end
      end  % End IF
      childNode = childNode.getNextSibling;
   end  % End WHILE

   if itemFound
      break;
   else
      infoCbk = '';
   end
end  % End FOR

disp(sprintf('Item "%s" has a callback of "%s".', ...
             infoLabel, infoCbk))