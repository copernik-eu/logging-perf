#
log4j_version = "2.23.1"
logback_version = "1.5.6"

set terminal svg size 800, 600 dynamic
set title "Log4j async logger vs Logback async appender"
#set boxwidth 0.3
#set style fill solid
set xtics 3,4,63
set xlabel "Logging threads"
set yrange [0:600]
set ylabel "MB/s"
plot 'logback.dat' smooth bezier t "Logback ".logback_version, 'log4jAsyncLogger.dat' smooth bezier t "Log4j ".log4j_version
