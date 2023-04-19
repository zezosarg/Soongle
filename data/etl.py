import pandas as pd

df = pd.read_csv('spotify_millsongdata.csv') # parse csv to a dataframe

df = df.drop('link', axis=1)    # drop link column

df = df.iloc[1: , :]	# drop first row

df = df.sample(n = 500) # pick n random samples

print(df)

df.to_csv('corpus.csv') # export to csv
