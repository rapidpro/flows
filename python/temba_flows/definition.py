from __future__ import absolute_import, unicode_literals

import json

from enum import Enum


class Flow(object):

    class Type(Enum):
        MESSAGE = 1
        IVR = 2
        SURVEY = 3

    @classmethod
    def from_json(cls, json_str):
        json_obj = json.loads(json_str)
        json_flow = json_obj['flow']

        # TODO
        return Flow()


