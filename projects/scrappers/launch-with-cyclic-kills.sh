#!/bin/bash
# This script performs a (launch-wait-kill) loop.
# The loop exits when the script has terminate in a given time.
#
# i.e. This script launches another script and kills if it is
# not finished after some time and launches it again and kills it again if
# it is not finished after some time.. and so on until the script finishes.
function display_help_and_quit {
	cat << EOF
Here is some help for you..
Script params:
	-s .. (or --script ..)
		The script path to launch
	-t .. (or --time ..)
		time in second between each kill
	-l .. (or --log ..)
		Path of the log file. Default: log.txt
	-h (or --help)
		show this help
EOF
	exit 1
}

# -----------------------------------------------------------------------------------
# Get script params
logPath="log.txt"
while [[ $# > 0 ]]
do
	key="$1"
	case $key in
	    -s|--script)
		    script="${2}"
			shift
		    ;;
	    -t|--time)
		    sleepTime="${2}"
			shift
		    ;;
	    -l|--log)
		    logPath="${2}"
			shift
		    ;;
	    -h|--help)
		    display_help_and_quit
		    ;;
	    *)
	        echo -e "Unknown param $1"
		    display_help_and_quit
	    	;;
	esac
	shift # past argument or value
done

if [ -z $script ] ; then
	echo -e "error: You have to specify the -s option."
	display_help_and_quit
fi

if [ -z $sleepTime ] ; then
	echo -e "error: You have to specify the -t option."
	display_help_and_quit
fi

# -----------------------------------------------------------------------------------
# Main code:
#
# Create the log file
if [ ! -f "${logPath}" ]; then
    touch "${logPath}"
fi

# While the script hasn't finished
stay_in_loop=1
while [ $stay_in_loop -ne 0 ]; do
	# Launch the script in background
	msg="[launch-with-cyclic-kills.sh] Process lauched"
	echo $msg
	echo $msg >> "${logPath}"
	phantomjs "${script}" >> "${logPath}" &
	last_pid=$!

	# Sleep a bit
	sleep $sleepTime

	# Test if it has finished
	kill -s 0 $last_pid

	if [ $? -eq 1 ]; then
		if [ -f "finished.info" ]; then
			msg="[launch-with-cyclic-kills.sh] The process has published a finished.info"
			echo $msg
			echo $msg >> "${logPath}"

			# Leave the loop
			stay_in_loop=0
		else
			msg="[launch-with-cyclic-kills.sh] Process has died"
			echo $msg
			echo $msg >> "${logPath}"
		fi
	fi

	# kill the process
	kill -KILL $last_pid
	msg="[launch-with-cyclic-kills.sh] Kill the process (even if it already died)"
	echo $msg
	echo $msg >> "${logPath}"
 done

msg="[launch-with-cyclic-kills.sh] Successfully ended!"
echo $msg
echo $msg >> "${logPath}"

exit 0
