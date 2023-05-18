import pandas as pd

df = pd.read_csv('spotify_millsongdata.csv') # parse csv to a dataframe

df = df.drop('link', axis=1)    # drop link column

df = df.sample(n = 10000) # pick n random samples MAX 40K

df.to_csv('corpus.csv', header=False) # export to csv without header
