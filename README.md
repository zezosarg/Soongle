**Introduction**

The goal of the project is a comlete web application that provides an information retrieval system for searching information about songs.
The main techologies used are the Lucene library and the Spring Boot framework.


**Corpus**

The corpus is a subset of the spotify million song dataset provided by kaggle at https://www.kaggle.com/datasets/notshrirang/spotify-million-song-dataset.
Using a python script, the data was loaded and some of the songs were randomly chosen.
The extracted dataset uses a csv fomat as well as the original.


**Text analyser and index creation**

For the analysis of text, the standard analyser will be used. This will remove stopwords and lowercase the tokens generated.
The fields of the documents are: Artist, Title, Lyrics.
The indexes to be created will be of type FSDirectory so as to be persistently stored to disk.


**Search**

The system will support keyword based search.
Other types of searching include field search.
Moreover a record of the queries will be preserved to suggest alternative results.


**Result representation**

Results will be ranked based on affinity with the query.
They will be fetched 10 at a time uppon user request.
Search keywords will be highlighted in results.
A grouping feature upon the artist field is available. 
