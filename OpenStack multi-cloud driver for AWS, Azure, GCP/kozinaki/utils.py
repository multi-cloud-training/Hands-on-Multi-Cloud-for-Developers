# Copyright (c) 2016 CompuNova Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
import time
from functools import wraps


def timeout_call(wait_period, timeout):
    """
    This decorator calls given method repeatedly
    until it throws exception. Loop ends when method
    returns.
    """
    def _inner(f):
        @wraps(f)
        def _wrapped(*args, **kwargs):
            start = time.time()
            if '_timeout' in kwargs and kwargs['_timeout']:
                _timeout = kwargs['_timeout']
                end = start + _timeout
            else:
                end = start + timeout
            exc = None
            while(time.time() < end):
                try:
                    return f(*args, **kwargs)
                except Exception as exc:
                    time.sleep(wait_period)
            raise exc
        return _wrapped
    return _inner

# test
@timeout_call(wait_period=3, timeout=600)
def func():
    raise Exception('none')


if __name__ == "__main__":
    func(_timeout=3)