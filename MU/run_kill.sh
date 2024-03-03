#!/bin/bash
./main < $1 &
process_id=$!
sleep $2
kill -SIGTERM $process_id