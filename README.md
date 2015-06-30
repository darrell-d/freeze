# Freeze
A simple AWS Glacier Java command line AWS upload tool.


##Usage

>java -jar freeze.jar  upload /fridge/freezer/ice_cream.7z

You will be asked to enter your secret key and access key

*Note* these are stored in plain text at the moment in a .properties file

To view a list of your files run:
> java -jar freeze.jar list

To download a file after getting the archive ID from listing run:
> java -jar freeze.jar download XXXX

You need a minimum of 4 characters from the ID

All requests can take up to 4 hours per Amazon Glacier rules. If the request is interrupted then it must be restarted.

Coming soon

- Pick up an interrupted request to list
- allow multiple uploads
- Upload progress feedback
