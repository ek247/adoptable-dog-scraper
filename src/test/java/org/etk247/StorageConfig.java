package org.etk247;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;

import javax.inject.Singleton;

import io.quarkus.arc.DefaultBean;

@Singleton
public class StorageConfig {
    @Singleton
    @DefaultBean
    Storage localStorage() {
        return LocalStorageHelper.getOptions().getService();
    }
}
