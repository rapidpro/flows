from os import path
from setuptools import setup

here = path.abspath(path.dirname(__file__))


setup(
    name='excellent',
    version='0.1',
    description='Python implementation of the RapidPro templating system',
    url='https://github.com/rapidpro',

    author='Nyaruka',
    author_email='code@nyaruka.com',

    license='BSD',

    classifiers=[
        'Development Status :: 1 - Planning',
        'Intended Audience :: Developers',
        'Topic :: Software Development :: Libraries',
        'License :: OSI Approved :: BSD License',
        'Programming Language :: Python :: 2',
        'Programming Language :: Python :: 2.6',
        'Programming Language :: Python :: 2.7',
    ],

    keywords='rapidpro templating',
    packages=['temba'],
    install_requires=['pytz'],

    test_suite='nose.collector',
    tests_require=['nose', 'mock', 'coverage'],
)
