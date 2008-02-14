keeponly2 :: String -> String
keeponly2 s = unlines $ map (unwords.tail) $ filter (\l -> length l == 3) $ map words $ lines s
