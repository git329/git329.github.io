scan.setMaxResultSize vs scan.setCaching
==========================
* .setCaching => .setNumberOfRowsFetchSize (客户端每次 rpc fetch 的行数)
* .setMaxResultSize => .setMaxResultByteSize （客户端缓存的最大字节数）


Reverse Timestamp
===============

Long.MAX_VALUE – timestamp

https://stackoverflow.com/questions/10638996/reverse-timestamp
http://hbase.apache.org/0.94/book/rowkey.design.html


FuzzyRowFilter
============
提供性能？ 比SubstringComparator？