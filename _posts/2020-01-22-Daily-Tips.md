How to use top
=============
```
# top -p 20846 -H -b -d 5 -n 3 >top_summary.txt
# top -p 20846 -H -b -d 5 >> top_summary.txt
```
-p: 对应的是进程id
-H：可以看到线程信息
-b：batch mode，这样输出到文件的才不是乱码
-d：refresh间隔，默认top是没3s，refresh一次
-n :保存几次snapshot

How to use timeout
==============
```
# timeout -sHUP 10m bash -c "while true;do curl http://localhost:8093/test;done"
```

Convert Decimal to Hex
==========
比如top得到的pid是20913,但是threadump得到的是nid=0x51b，是16进制的
```bash
echo "obase=16;20913"|bc
```