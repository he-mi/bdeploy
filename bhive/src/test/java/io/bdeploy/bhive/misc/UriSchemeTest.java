package io.bdeploy.bhive.misc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.UriBuilder;

class UriSchemeTest {

    @Test
    void testUriScheme() {
        URI u1 = UriBuilder.fromUri("bhive://host:1234").build();
        assertThat(u1.getScheme(), is("bhive"));

        URI u2 = Paths.get("/my/path").toUri();
        assertThat(u2.getScheme(), is("file"));

        URI u3 = UriBuilder.fromUri("jar:" + Paths.get("/my/path.zip").toUri()).build();
        assertThat(u3.getScheme(), is("jar"));
    }
}
