from setuptools import setup, find_packages


def _is_requirement(line):
    """Returns whether the line is a valid package requirement."""
    line = line.strip()
    return line and not (line.startswith("-r") or line.startswith("#"))


def _read_requirements(filename):
    """Returns a list of package requirements read from the file."""
    requirements_file = open(filename).read()
    return [line.strip() for line in requirements_file.splitlines()
            if _is_requirement(line)]


required_packages = _read_requirements("requirements/base.txt")
test_packages = _read_requirements("requirements/tests.txt")

setup(
    name='rapidpro-flows',
    version='1.2.1',
    description='Python implementation of the RapidPro flow engine',
    url='https://github.com/rapidpro/flows',

    author='Nyaruka',
    author_email='code@nyaruka.com',

    license='BSD',

    classifiers=[
        'Development Status :: 4 - Beta',
        'Intended Audience :: Developers',
        'Topic :: Software Development :: Libraries',
        'License :: OSI Approved :: BSD License',
        'Programming Language :: Python :: 2',
    ],

    keywords='rapidpro flows',
    packages=find_packages(),
    install_requires=required_packages,

    test_suite='nose.collector',
    tests_require=required_packages + test_packages,
)
