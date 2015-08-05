package expr.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import expr.Version;

public class TestVersion {
	
	@Test(expected=IllegalArgumentException.class)
	public void testMinorMustBePositive() {
		Version.of(-1, 2, 3);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMajorMustBePositive() {
		Version.of(1, -2, 3);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPatchMustBePositive() {
		Version.of(1, 2, -3);
	}
	
	@Test(expected=NullPointerException.class)
	public void testReleaseMustBeNonNull() {
		Version.of(0, 0, 0, null, "string");
	}
	
	@Test(expected=NullPointerException.class)
	public void testMetadataMustBeNonNull() {
		Version.of(0, 0, 0, "string", null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFailureOnIllegalMetadata() {
		Version.of(3, 4, 2, "", "ä");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFailureOnIllegalRelease() {
		Version.of(6, 2, 1, "?");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFailureOnLeadingZeroesInNumber() {
		Version.of(1, 0, 2, "beta.002");
	}
	
	@Test
	public void testFieldsAreSavedCorrectly() {
		int major = 4, minor = 2, patch = 6;
		String release = "beta.3", metadata = "30460";
		Version v = Version.of(major, minor, patch, release, metadata);
		assertEquals(major, v.major());
		assertEquals(minor, v.minor());
		assertEquals(patch, v.patch());
		assertEquals(release, v.release());
		assertEquals(metadata, v.metadata());
	}
	
	@Test
	public void testEqualsOnEqualObjects() {
		assertEquals(Version.of(1, 2, 3, "a", "b"), Version.of(1, 2, 3, "a", "b"));
		assertEquals(Version.of(1, 2, 3), Version.of(1, 2, 3, "", ""));
	}
	
	@Test
	public void testEqualsOnUnequalObjects() {
		assertNotEquals(Version.of(1, 2, 3), Version.of(3, 2, 1));
		assertNotEquals(Version.of(1, 2, 3), Version.of(1, 2, 3, "a", "b"));
	}
	
	@Test
	public void testHashCodeOnEqualObjects() {
		assertEquals(Version.of(1, 2, 3, "a", "b").hashCode(),
				Version.of(1, 2, 3, "a", "b").hashCode());
	}
	
	@Test
	public void testHashCodeOnUnequalObjects() {
		assertNotEquals(Version.of(1, 2, 3).hashCode(),
				Version.of(1, 2, 3, "a", "b").hashCode());
	}
	
	@Test
	public void testToString() {
		assertEquals("4.1.3", Version.of(4, 1, 3).toString());
		assertEquals("1.0.2-beta", Version.of(1, 0, 2, "beta").toString());
		assertEquals("2.1.5-alpha+M450", Version.of(2, 1, 5, "alpha", "M450").toString());
		assertEquals("3.6.1+b0802", Version.of(3, 6, 1, "", "b0802").toString());
	}
	
	@Test
	public void testSimpleParse() {
		assertEquals("1.2.3", Version.parse("1.2.3").toString());
	}
	
	@Test
	public void testParseWithOptionalsPresent() {
		assertEquals("1.2.3-re+me", Version.parse("1.2.3-re+me").toString());
	}
	
	@Test
	public void testParseWithStringsWithHyphens() {
		assertEquals("1.2.3-beta-release+some-metadata",
				Version.parse("1.2.3-beta-release+some-metadata").toString());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParseFailureWithNegativeNumber() {
		Version.parse("1.-4.2");
	}
	
	@Test(expected=NumberFormatException.class)
	public void testParseFailureWithMalformedNumber() {
		Version.parse("3.7m.2");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParseFailureWithEmptyMetadata() {
		Version.parse("1.0.3-r+");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParseFailureWithEmptyRelease() {
		Version.parse("2.1.3-+metadata");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParseFailureWithIllegalReleaseAndMetadata() {
		Version.parse("1.0.1-ðêłẽäßé+mëþàðâþä");
	}
	
	@Test
	public void testEqualVersionsCompatible() {
		assertTrue(Version.of(0, 3, 2).isCompatible(Version.of(0, 3, 2)));
		assertTrue(Version.of(1, 3, 2).isCompatible(Version.of(1, 3, 2)));
		assertTrue(Version.of(2, 0, 1, "a").isCompatible(Version.of(2, 0, 1, "a")));
	}
	
	@Test
	public void testVersionsWithSameMajorCompatible() {
		assertTrue(Version.of(2, 1, 2).isCompatible(Version.of(2, 0, 3)));
	}
	
	@Test
	public void testZeroOverridingCompatibility() {
		assertFalse(Version.of(0, 1, 2).isCompatible(Version.of(0, 2, 3)));
	}
	
	@Test
	public void testReleaseOverridingCompatibility() {
		assertFalse(Version.of(2, 5, 1).isCompatible(Version.of(2, 5, 1, "b")));
		assertFalse(Version.of(2, 5, 1, "a").isCompatible(Version.of(2, 5, 1, "b")));
	}
	
	@Test
	public void testMetadataDoesNotAffectCompatibility() {
		assertTrue(Version.of(4, 3, 2).isCompatible(Version.of(4, 3, 2, "", "metadata")));
		assertFalse(Version.of(0, 1, 2).isCompatible(Version.of(0, 2, 3, "", "metadata")));
	}
	
	@Test
	public void testVersionNumberComparison() {
		assertTrue(Version.of(3, 0, 0).compareTo(Version.of(2, 0, 0)) > 0);
		assertTrue(Version.of(1, 1, 0).compareTo(Version.of(1, 0, 0)) > 0);
		assertTrue(Version.of(1, 0, 1).compareTo(Version.of(1, 0, 0)) > 0);
		assertTrue(Version.of(1, 1, 0).compareTo(Version.of(1, 0, 1)) > 0);
		assertTrue(Version.of(1, 0, 0).compareTo(Version.of(1, 0, 0)) == 0);
	}
	
	@Test
	public void testVersionComparisonWithRelease() {
		assertTrue(Version.of(1, 0, 0, "r").compareTo(Version.of(1, 0, 0)) < 0);
		assertTrue(Version.of(1, 0, 0, "a").compareTo(Version.of(1, 0, 0, "a")) == 0);
	}
	
	@Test
	public void testLexicalComparisonOfRelease() {
		assertTrue(Version.of(1, 0, 0, "b").compareTo(Version.of(1, 0, 0, "a")) > 0);
	}
	
	@Test
	public void testLargerFieldComesFirstWhenFirstPartsEqual() {
		Version.of(1, 0, 0, "aa").compareTo(Version.of(1, 0, 0, "a"));
		Version.of(1, 0, 0, "a").compareTo(Version.of(1, 0, 0, "aa"));
	}
	
	@Test
	public void testNumericalComparisonOfRelease() {
		assertTrue(Version.of(1, 0, 0, "a.4").compareTo(Version.of(1, 0, 0, "a.2")) > 0);
	}
	
	@Test
	public void testReleaseWithMoreFieldsIsLarger() {
		assertTrue(Version.of(1, 0, 0, "alpha.1").compareTo(Version.of(1, 0, 0, "alpha")) > 0);
	}
	
	@Test
	public void testReleaseComparisonWithMultipleFields() {
		assertTrue(Version.of(1, 0, 0, "alpha.1.1").compareTo(Version.of(1, 0, 0, "alpha.1.0")) > 0);
	}
	
	@Test
	public void testNumericalFieldIsLessThanNonNumeric() {
		assertTrue(Version.of(1, 0, 0, "alpha.1").compareTo(Version.of(1, 0, 0, "alpha.a")) < 0);
	}
	
	@Test
	public void testMetadataDoesntAffectComparison() {
		assertTrue(Version.of(3, 2, 0, "", "meta").compareTo(Version.of(3, 2, 0)) == 0);
	}
	
	@Test
	public void testSort() {
		List<Version> versions = Arrays.asList(
				Version.of(1, 0, 1),
				Version.of(1, 0, 2),
				Version.of(1, 1, 0),
				Version.of(1, 1, 1, "alpha"),
				Version.of(1, 1, 1, "alphas"),
				Version.of(1, 1, 1, "alpha.1"),
				Version.of(1, 1, 1, "alpha.3"),
				Version.of(1, 1, 1, "alpha.a"),
				Version.of(1, 1, 1, "alpha.b"),
				Version.of(1, 1, 1, "beta.1"),
				Version.of(1, 1, 1, "alpha.1.2"),
				Version.of(1, 1, 1));
		
		List<Version> sorted = new ArrayList<>(versions);
		Collections.sort(sorted);
		
		assertEquals(sorted, versions);
	}
	
}