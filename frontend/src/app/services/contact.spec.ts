import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { ContactService } from './contact';

describe('ContactService', () => {
  let service: ContactService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient() // Configura il provider fittizio per il test
      ]
    });
    service = TestBed.inject(ContactService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
