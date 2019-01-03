package org.monarchinitiative.lr2pg.io;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Timestamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.lr2pg.likelihoodratio.PhenotypeLikelihoodRatioTest;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.schema.v1.PhenoPacket;
import org.phenopackets.schema.v1.core.*;
import org.phenopackets.schema.v1.io.PhenoPacketFormat;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static org.junit.jupiter.api.Assertions.*;

public class PhenopacketImporterTest {

    private static String phenopacketJsonString;

    @BeforeAll
    static void setup() throws ParseException, IOException,NullPointerException {
        ClassLoader classLoader = PhenotypeLikelihoodRatioTest.class.getClassLoader();
        URL resource = classLoader.getResource("spherocytosis.json");
        if (resource==null){
            throw new FileNotFoundException("Could not find phenopacket file");
        }
        String phenopacketPath = resource.getFile();
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(phenopacketPath));
        JSONObject jsonObject = (JSONObject) obj;
        phenopacketJsonString = jsonObject.toJSONString();
    }


    @Test
    void testInputPhenopacketNotNull() throws IOException  {
       PhenoPacket fromJson = PhenoPacketFormat.fromJson(phenopacketJsonString);
       assertNotNull(fromJson);
    }

    @Test
    void testNumberOfHpoTermsImported() throws IOException {
        PhenoPacket fromJson = PhenoPacketFormat.fromJson(phenopacketJsonString);
        PhenopacketImporter importer = new PhenopacketImporter(fromJson);
        List<TermId> nonnegatedTerms = importer.getHpoTerms();
        TermId reticulocytosis = TermId.of("HP:0001923");
        assertTrue(nonnegatedTerms.contains(reticulocytosis));
        // the following term is negated in the phenopacket
        TermId Hepatomegaly = TermId.of("HP:0002240");
        assertFalse(nonnegatedTerms.contains(Hepatomegaly));
        TermId Splenomegaly = TermId.of("HP:0001744");
        assertTrue(nonnegatedTerms.contains(Splenomegaly));
        TermId Jaundice= TermId.of("HP:0000952");
        assertTrue(nonnegatedTerms.contains(Jaundice));
        TermId Spherocytosis= TermId.of("HP:0004444");
        assertTrue(nonnegatedTerms.contains(Spherocytosis));
        // There are a total of four non-negated terms in the phenopacket
        assertEquals(4,nonnegatedTerms.size());
    }

    /** The phenopacket contains a single negated term. */
    @Test
    void testGetOneNegatedTerm() throws IOException {
        PhenoPacket fromJson = PhenoPacketFormat.fromJson(phenopacketJsonString);
        PhenopacketImporter importer = new PhenopacketImporter(fromJson);
        List<TermId> negatedTerms = importer.getNegatedHpoTerms();
        TermId Hepatomegaly = TermId.of("HP:0002240");
        assertTrue(negatedTerms.contains(Hepatomegaly));
        assertEquals(1,negatedTerms.size());
    }


    @Test
    void testGetPathToVcfFile() throws IOException {
        PhenoPacket fromJson = PhenoPacketFormat.fromJson(phenopacketJsonString);
        PhenopacketImporter importer = new PhenopacketImporter(fromJson);
        String expectedVcfPath="/home/user/example.vcf"; // as in the Json phenopacket file
        assertEquals(expectedVcfPath,importer.getVcfPath());
        String expectedGenomeBuild="GRCH_38"; // as in the Json phenopacket file
        assertEquals(expectedGenomeBuild,importer.getGenomeAssembly());
    }


}