import { TestBed } from "@angular/core/testing";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { FileSystemDataService } from "../data/file-system-data.service";
import { TaskDataService } from "../data/task-data.service";
import { FileSystemBusinessService } from "./file-system.service";

describe("FileSystemBusinessService", () => {
    let service: FileSystemBusinessService;
    let fileSystemDataServiceSpy: jasmine.SpyObj<FileSystemDataService>;
    let taskDataServiceSpy: jasmine.SpyObj<TaskDataService>;
    let messageServiceSpy: jasmine.SpyObj<MessageService>;
    let translateServiceSpy: jasmine.SpyObj<TranslateService>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                FileSystemBusinessService,
                {
                    provide: FileSystemDataService,
                    useValue: jasmine.createSpyObj("FileSystemDataService", [
                        "downloadResultsFile",
                    ]),
                },
                {
                    provide: TaskDataService,
                    useValue: jasmine.createSpyObj("TaskDataService", ["getTask"]),
                },
                {
                    provide: MessageService,
                    useValue: jasmine.createSpyObj("MessageService", ["add"]),
                },
                {
                    provide: TranslateService,
                    useValue: jasmine.createSpyObj("TranslateService", ["instant"]),
                },
            ],
        });

        service = TestBed.inject(FileSystemBusinessService);
        fileSystemDataServiceSpy = TestBed.inject(
            FileSystemDataService,
        ) as jasmine.SpyObj<FileSystemDataService>;
        taskDataServiceSpy = TestBed.inject(
            TaskDataService,
        ) as jasmine.SpyObj<TaskDataService>;
        messageServiceSpy = TestBed.inject(
            MessageService,
        ) as jasmine.SpyObj<MessageService>;
        translateServiceSpy = TestBed.inject(
            TranslateService,
        ) as jasmine.SpyObj<TranslateService>;
    });

    afterEach(() => {
        jasmine.clock().uninstall?.();
    });

    it("should be create", () => {
        expect(service).toBeTruthy();
    });
});
