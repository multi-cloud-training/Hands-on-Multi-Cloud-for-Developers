#!/usr/bin/env python
import os
import sys

import environ


if __name__ == "__main__":
    django_settings_module = environ.Env.read_env('.env')
    os.environ.setdefault("DJANGO_SETTINGS_MODULE", django_settings_module)

    try:
        from django.core.management import execute_from_command_line
    except ImportError:
        # The above import may fail for some other reason. Ensure that the
        # issue is really that Django is missing to avoid masking other
        # exceptions on Python 2.
        try:
            import django  # noqa
        except ImportError:
            raise ImportError(
                "Couldn't import Django. Are you sure it's installed and "
                "available on your PYTHONPATH environment variable? Did you "
                "forget to activate a virtual environment?"
            )

        raise

    # This allows easy placement of apps within the interior
    # cloud_on_the_fly directory.
    current_path = os.path.dirname(os.path.abspath(__file__))
    sys.path.append(os.path.join(current_path, "cloud_on_the_fly"))

    execute_from_command_line(sys.argv)
