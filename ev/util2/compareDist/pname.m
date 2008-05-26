function pname(name,mat)

mesh(mat)



for i=1:length(name)
    for j=1:length(name)
        text(j,i,mat(i,j),[cell2mat(name(i)) '-' cell2mat(name(j))])
    end
end
