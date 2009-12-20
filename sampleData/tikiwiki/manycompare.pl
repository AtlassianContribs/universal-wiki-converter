#!/usr/bin/perl -w

## Idea is to create an easy way to run all the compare scripts for a range

use Getopt::Std;

getopts("s:e:", \%opts);

if ($opts{"s"} && $opts{"e"}) {
	$start = $opts{"s"};
	$end = $opts{"e"};
	print "range: start = $start, end = $end\n";
	for ($i = $start; $i <= $end; $i++) {
		print "SyntaxTikiwiki-Input$i\n";
		system("./compare.sh $i");
	}
}
else {
	usage();
	exit;
}

sub usage {
	my $message = "\nUsage: manycompare.pl -s startnumber -e endnumber\n" . "\n" . "\tRuns the compare script on all test files with numbers\n\tlisted in the given range, inclusive.\n\tEx: manycompare.pl -s 1 -e 5 will run compare on\n\tSampleTikiwiki-Input1.txt through SampleTikiwiki-Input5.txt\n\n" ;
	print $message;
}
