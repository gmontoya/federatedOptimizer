from collections import OrderedDict

import json
import sys

def main(argv):
    file=argv[1]
    with open(file, 'r') as f:
        l = f.read()
        if len(l) > 0:
            m = json.loads(l)
            n = m['results']['bindings']
            for e in n:
                f = dict()
                for var in e:
                    val = e[var][u'value']
                    f[var] = val
                f = OrderedDict(sorted(f.items(), key=lambda t: t[0]))
                print f

if __name__ == '__main__':
    main(sys.argv)
