This repository contains all code from the NOA project (noa.wp.hs-hannover.de). More documentation will be added soon.

# Add Wikimedia Categories
This programs finds all distinct Wikipedia Categories in a MongoDB, writes them into a new database and adds the corresponding categories from Wikimedia Commons as well as Wikidata items. It then adds the Wikimedia Commons categories to the original database. The created database can be deleted. The wikidata items are not added to the original database. This can be done with small changes to the source code.
