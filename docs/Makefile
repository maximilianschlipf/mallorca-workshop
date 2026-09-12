# Minimal makefile for Sphinx documentation
#

# You can set these variables from the command line, and also
# from the environment for the first two.
# -W macht jede Warnung zum Fehler -- damit schlagen schema-fremde IDs und
# ungueltige Statuswerte den Build, statt nur eine Zeile ins Log zu schreiben.
# Sphinx sammelt dabei erst alle Verstoesse ein, statt beim ersten zu stoppen.
SPHINXOPTS    ?= -W
SPHINXBUILD   ?= ../.venv/bin/sphinx-build
SOURCEDIR     = source
BUILDDIR      = build

# Put it first so that "make" without argument is like "make help".
help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

.PHONY: help Makefile

# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile
	@$(SPHINXBUILD) -M $@ "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)
