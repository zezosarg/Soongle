**Introduction**

The goal of the project is a complete web application that provides an information retrieval system for searching information about songs.
The main techologies used are the Lucene library and the Spring Boot framework.


**Corpus**

The corpus is a subset of the spotify million song dataset provided by kaggle at https://www.kaggle.com/datasets/notshrirang/spotify-million-song-dataset.
Using a python script, the data was loaded and some of the songs were randomly chosen.
The extracted dataset uses a csv format as does the original.


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
They will be fetched 10 at a time upon user request.
Search keywords will be highlighted in results.
A grouping feature upon the artist field is available as well as semantic search.


**Usage**

Upon visiting the home page, the regular and word2vec indexes are built enabling all types of search.
There is an option to rebuild the regular lucene index in case a problem occurs.
The user can select between the three types of searches (regular, group by artist, semantic) via a dropdown list.
Autocompletion is supported based on search history.
The user can select the fields that the search will utilize typing 'field:query' separated with AND/OR operators.
When viewing results, the user can request additional or return to the home page.
