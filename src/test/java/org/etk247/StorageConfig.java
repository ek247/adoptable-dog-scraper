package org.etk247;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
public class StorageConfig {
    @ApplicationScoped
    @DefaultBean
    Storage localStorage() {
        return LocalStorageHelper.getOptions().getService();
    }
}
