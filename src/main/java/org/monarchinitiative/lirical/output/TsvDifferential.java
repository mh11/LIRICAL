package org.monarchinitiative.lirical.output;

import org.monarchinitiative.lirical.analysis.Gene2Genotype;
import org.monarchinitiative.lirical.likelihoodratio.TestResult;
import org.monarchinitiative.lirical.vcf.SimpleVariant;

import java.util.List;
import java.util.stream.Collectors;

public class TsvDifferential {

    private final static String EMPTY_STRING="";
    private final static String NOT_AVAILABLE="n/a";
    private final String diseaseName;
    private final String diseaseCurie;
    private final int rank;
    private final String pretestprob;
    private final String posttestprob;
    private final double compositeLR;
    private final String entrezGeneId;

    private List<SimpleVariant> varlist;
    private String varString=NOT_AVAILABLE;
    /** Set this to yes as a flag for the template to indicate we can show some variants. */
    private String hasVariants="No";

    private String geneSymbol=EMPTY_STRING;

    public TsvDifferential(TestResult result) {
        this.diseaseName=prettifyDiseaseName(result.getDiseaseName());
        this.diseaseCurie=result.getDiseaseCurie().getValue();
        this.rank=result.getRank();
        if (result.getPosttestProbability()>0.9999) {
            this.posttestprob=String.format("%.5f%%",100*result.getPosttestProbability());
        } else if (result.getPosttestProbability()>0.999) {
            this.posttestprob=String.format("%.4f%%",100*result.getPosttestProbability());
        } else if (result.getPosttestProbability()>0.99) {
            this.posttestprob=String.format("%.3f%%",100*result.getPosttestProbability());
        } else {
            this.posttestprob=String.format("%.2f%%",100*result.getPosttestProbability());
        }

        double ptp=result.getPretestProbability();
        if (ptp < 0.001) {
            this.pretestprob = String.format("1/%d",Math.round(1.0/ptp));
        } else {
            this.pretestprob = String.format("%.6f",ptp);
        }
        this.compositeLR=result.getCompositeLR();
        if (result.hasGenotype()) {
            this.entrezGeneId = result.getEntrezGeneId().getValue();
        } else {
            this.entrezGeneId=NOT_AVAILABLE;
        }
    }

    void addG2G(Gene2Genotype g2g) {
        this.geneSymbol=g2g.getSymbol();
        this.hasVariants="yes";
        this.varlist =g2g.getVarList();
        this.varString=varlist.stream().map(SimpleVariant::toString).collect(Collectors.joining("; "));
    }

    public String getVarString() {
        return varString;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public String getDiseaseCurie() {
        return diseaseCurie;
    }


    public int getRank() {
        return rank;
    }

    public String getPretestprob() {
        return pretestprob;
    }

    public String getPosttestprob() {
        return posttestprob;
    }

    public double getCompositeLR() {
        return compositeLR;
    }

    public String getEntrezGeneId() {
        return entrezGeneId;
    }

    public List<SimpleVariant> getVarlist() {
        return varlist;
    }

    public String getHasVariants() {
        return hasVariants;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    /**
     * We are getting the disease names from OMIM (actually from our small files), and so some of them are long and
     * unweildly strings such as the following:
     * {@code }#101200 APERT SYNDROME;;ACROCEPHALOSYNDACTYLY, TYPE I; ACS1;;ACS IAPERT-CROUZON DISEASE,
     * INCLUDED;;ACROCEPHALOSYNDACTYLY, TYPE II, INCLUDED;;ACS II, INCLUDED;;VOGT CEPHALODACTYLY, INCLUDED}. We want to
     * remove any leading numbers and only show the first part of the name (before the first ";;").
     * @param originalName original possibly verbose disease name with synonyms
     * @return prettified disease name intended for display on HTML page
     */
    private String prettifyDiseaseName(String originalName) {
        int i=originalName.indexOf(";;");
        if (i>0) {
            originalName=originalName.substring(0,i);
        }
        i=0;
        while (originalName.charAt(i)=='#' || Character.isDigit(originalName.charAt(i)) || Character.isWhitespace(originalName.charAt(i))) {
            i++;
            if (i>=originalName.length()) break;
        }
        return originalName.substring(i);
    }
}
