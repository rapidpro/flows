from __future__ import absolute_import, unicode_literals

import pkg_resources

MONTHS_BY_ALIAS = {}

alias_file = pkg_resources.resource_string(__name__, 'month.aliases').decode('UTF-8', 'replace')

month = 1
for line in alias_file.split('\n'):
    for alias in line.split(','):
        MONTHS_BY_ALIAS[alias] = month
    month += 1
