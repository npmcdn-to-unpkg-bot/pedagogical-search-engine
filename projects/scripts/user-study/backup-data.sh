#!/bin/bash

mysqldump -u "$1" -p$2 wikichimp messages searches clicks classifications > user_study_backup.sql