# SimpleGlacier
A simple AWS Glacier Java command line AWS upload tool.


##Usage

>simpleglacier upload filepath

You will be asked to enter your secret key and acess key

*Note* these are stored in plain text at the moment in a .properties file

To view a list of your files run:
> simpleglacier list

This process can take up to 4 hours as per Glacier rules. If the process is interupted then it must be queued up again.

Coming soon

- Pick up an interrupted request to list
- Keep a local results from last file listing
- allow multiple uploads
- Upload progress feedback