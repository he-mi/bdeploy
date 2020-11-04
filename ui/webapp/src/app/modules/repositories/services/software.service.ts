import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { DeviceDetectorService } from 'ngx-device-detector';
import { Observable } from 'rxjs';
import { ManifestKey, OperatingSystem } from '../../../models/gen.dtos';
import { ConfigService } from '../../core/services/config.service';
import { Logger, LoggingService } from '../../core/services/logging.service';
import { DownloadService } from '../../shared/services/download.service';
import { SoftwareRepositoryService } from './software-repository.service';

@Injectable({
  providedIn: 'root',
})
export class SoftwareService {
  private log: Logger = this.loggingService.getLogger('SoftwareRepositoryService');

  constructor(
    private cfg: ConfigService,
    private http: HttpClient,
    private loggingService: LoggingService,
    private downloadService: DownloadService,
    private deviceService: DeviceDetectorService
  ) {}

  public listSoftwares(
    softwareRepositoryName: string,
    listProducts: boolean,
    listGeneric: boolean
  ): Observable<ManifestKey[]> {
    const url: string = this.buildSoftwareUrl(softwareRepositoryName);
    let params = new HttpParams();
    params = listProducts ? params.set('products', 'true') : params;
    params = listGeneric ? params.set('generic', 'true') : params;
    this.log.debug('listSoftwares: ' + url);
    return this.http.get<ManifestKey[]>(url, { params: params });
  }

  public getSoftwareDiskUsage(softwareRepositoryName: string, manifestkeyName: string): Observable<string> {
    const url = this.buildSoftwareUrl(softwareRepositoryName) + '/' + manifestkeyName + '/diskUsage';
    this.log.debug('getSoftwareDiskUsage: ' + url);
    return this.http.get(url, { responseType: 'text' });
  }

  public deleteSoftwareVersion(softwareRepositoryName: string, key: ManifestKey) {
    const url: string = this.buildSoftwareNameTagUrl(softwareRepositoryName, key);
    this.log.debug('deleteSoftwareVersion: ' + url);
    return this.http.delete(url);
  }

  public createSoftwareZip(softwareRepositoryName: string, key: ManifestKey): Observable<string> {
    const url = this.buildSoftwareNameTagUrl(softwareRepositoryName, key) + '/zip';
    this.log.debug('createSoftwareZip: ' + url);
    return this.http.get(url, { responseType: 'text' });
  }

  public downloadSoftware(token: string): string {
    return this.downloadService.createDownloadUrl(token);
  }

  public getSoftwareUploadUrl(softwareRepositoryName: string): string {
    return this.buildSoftwareUrl(softwareRepositoryName) + '/upload';
  }

  public getSoftwareUploadRaw(softwareRepositoryName: string): string {
    return this.buildSoftwareUrl(softwareRepositoryName) + '/upload-raw-content';
  }

  private buildSoftwareUrl(softwareRepositoryName: string): string {
    return this.cfg.config.api + SoftwareRepositoryService.BASEPATH + '/' + softwareRepositoryName + '/content';
  }

  private buildSoftwareNameTagUrl(softwareRepositoryName: string, key: ManifestKey): string {
    return this.buildSoftwareUrl(softwareRepositoryName) + '/' + key.name + '/' + key.tag;
  }

  public getRunningOs() {
    const runningOs = this.deviceService.os;
    if (runningOs === 'Windows') {
      return OperatingSystem.WINDOWS;
    } else if (runningOs === 'Linux') {
      return OperatingSystem.LINUX;
    } else if (runningOs === 'Mac') {
      return OperatingSystem.MACOS;
    }
    return OperatingSystem.UNKNOWN;
  }
}
