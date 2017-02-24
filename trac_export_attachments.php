<?php

/**
 * Dumps Trac wiki attachments in a format understandable by the UWC.
 *
 * This script assumes Trac uses a Postgres server.
 */

$dbhost = 'localhost'; // Trac DB host
$dbuser = 'trac'; // Trac DB username
$dbpass = ''; // Trac DB password
$dbname = 'trac'; // Trac DB name
$rootdir = '/tmp/trac-attachments'; // Attachments will be dumped into this folder
$tracenv = '/var/www/trac'; // Path to Trac environment


$rs = pg_query("SELECT id,filename FROM attachment WHERE type='wiki'") or die ('query failed ' . pg_last_error());

echo "#!/bin/bash\n";
while($row = pg_fetch_array($rs)) {
  $id = $row['id'];
  $name = escapeshellarg($row['filename']);
  $filedir = "$rootdir/$id";
  $filepath = escapeshellarg($filedir . '/' . $row['filename']);
  echo "echo mkdir -p $filedir\n";
  echo "mkdir -p $filedir\n";
  $command = "trac-admin $tracenv attachment export wiki:" . $id . " " . $name . " " . $filepath;
  echo "echo $command\n";
  echo $command . "\n";
}
pg_free_result($rs);
pg_close($conn);

